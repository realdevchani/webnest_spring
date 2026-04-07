package com.app.webnest.service;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.exception.GameJoinException;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class GameRoomLifecycleServiceImpl implements GameRoomLifecycleService {

    private final GameJoinService gameJoinService;
    private final GameRoomService gameRoomService;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ConcurrentHashMap<String, SessionInfo> sessionRegistry = new ConcurrentHashMap<>();

    private static final String DISCONNECT_KEY_PREFIX = "ws:disconnect:";
    private static final long DISCONNECT_TTL_SECONDS = 30;

    private final ScheduledExecutorService scheduledExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "ws-disconnect-cleanup");
                thread.setDaemon(true);
                return thread;
            });

    @Getter
    @AllArgsConstructor
    private static class SessionInfo {
        private final Long userId;
        private final Long gameRoomId;
    }


    @Override
    public void handleJoin(ChatMessageVO chatMessageVO) {
//    userId:roomId 로 레디스 key 확인
        Long gameRoomId = chatMessageVO.getGameRoomId();
        Long senderUserId = chatMessageVO.getUserSenderId();
        GameJoinVO gameJoinVO = new GameJoinVO(chatMessageVO);

        String disconnectKey = DISCONNECT_KEY_PREFIX + senderUserId + ":" + gameRoomId;

//        레디스에 키가 있다 == 잠깐 연결이 끊겼다 재접속 -> redis key 삭제만
        if(Boolean.TRUE.equals(redisTemplate.hasKey(disconnectKey))){
            redisTemplate.delete(disconnectKey);
            return;
        }

//        레디스에 키가 없다 == 실제 신규 입장
//        찾아온 DB에 해당 데이터가 없다면 -> 실제로 신규 입장 아니라면 기존 참여자
        Optional<GameJoinVO> existedGameJoin = gameJoinService.getGameJoinDTOByGameRoomId(gameJoinVO);
        if(existedGameJoin.isEmpty()){
//            신규입장시 -> 컬러 변경 + 호스트 아닐 때 호스트로 변경
            if(gameJoinVO.getGameJoinIsHost() == null){ gameJoinVO.setGameJoinIsHost(0); }
            gameJoinService.join(gameJoinVO);
            assignHostIfNeeded(gameRoomId);
        }else {
            updateTeamColorIfNeeded(existedGameJoin.get(), chatMessageVO.getUserSenderTeamcolor());
        }
    }

    @Override
    public void handleLeave(ChatMessageVO chatMessageVO) {
        GameJoinVO gameJoinVO = new GameJoinVO(chatMessageVO);
        Long gameRoomId = gameJoinVO.getGameRoomId();
        GameJoinVO currentUser = gameJoinService.getGameJoinDTOByGameRoomId(gameJoinVO)
                .orElseThrow(() -> new GameJoinException("나갈 유저를 찾지 못했습니다."));
        List<GameJoinVO> players = gameJoinService.getUserListByEntrancedTime(gameRoomId);

//        혼자 있을 경우 => 방 삭제
        if (players.size() <= 1) {
            gameJoinService.leave(gameJoinVO);
            gameRoomService.delete(gameRoomId);
            return;
        }

//        나가기 처리 + 호스트 동시에
        leaveUser(currentUser, players);
    }

    @Override
    public void handleDisconnect(String sessionId) {
        SessionInfo info = sessionRegistry.remove(sessionId);
//        null --> 정상 삭제됨
        if (info == null) return;

        Long userId = info.getUserId();
        Long gameRoomId = info.getGameRoomId();

        // Redis에 disconnect 마커 저장 (TTL 30초)
        String key = DISCONNECT_KEY_PREFIX + userId + ":" + gameRoomId;
        redisTemplate.opsForValue().set(key, sessionId, DISCONNECT_TTL_SECONDS, TimeUnit.SECONDS);

        scheduledExecutor.schedule(() -> {
            String existing = redisTemplate.opsForValue().get(key);
            if (existing != null && existing.equals(sessionId)) {
                redisTemplate.delete(key);
                performDisconnectCleanup(userId, gameRoomId);
            }
        }, DISCONNECT_TTL_SECONDS + 1, TimeUnit.SECONDS);
    }

    @Override
    public void registerSession(String sessionId, Long userId, Long gameRoomId) {
        sessionRegistry.put(sessionId, new SessionInfo(userId, gameRoomId));
    }

    @Override
    public void unregisterSession(String sessionId) {
        sessionRegistry.remove(sessionId);
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutor.shutdownNow();
    }

    private void assignHostIfNeeded(Long gameRoomId) {
        List<GameJoinDTO> allPlayers = gameJoinService.getPlayers(gameRoomId);
        boolean hasHost = allPlayers.stream().anyMatch(GameJoinDTO::isGameJoinIsHost);

        if (!hasHost && !allPlayers.isEmpty()) {
            GameJoinDTO firstPlayer = allPlayers.get(0);
            GameJoinVO hostVO = new GameJoinVO();
            hostVO.setUserId(firstPlayer.getUserId());
            hostVO.setGameRoomId(gameRoomId);
            hostVO.setGameJoinIsHost(1);
            gameJoinService.update(hostVO);
        }
    }

    private void performDisconnectCleanup(Long userId, Long gameRoomId) {
        ChatMessageVO leaveVO = new ChatMessageVO();
        leaveVO.setUserSenderId(userId);
        leaveVO.setGameRoomId(gameRoomId);

        // handleLeave와 동일한 정리 (직접 호출하지 않고 로직 재사용)
        GameJoinVO gameJoinVO = new GameJoinVO(leaveVO);
        Optional<GameJoinVO> currentUserOpt = gameJoinService.getGameJoinDTOByGameRoomId(gameJoinVO);
        if (currentUserOpt.isEmpty()) return;

        GameJoinVO currentUser = currentUserOpt.get();
        List<GameJoinVO> players = gameJoinService.getUserListByEntrancedTime(gameRoomId);

        if (players.size() <= 1) {
            gameJoinService.leave(gameJoinVO);
            gameRoomService.delete(gameRoomId);
            return;
        }
        leaveUser(currentUser, players);
    }
    private void leaveUser(GameJoinVO leaveUser, List<GameJoinVO> players){
        hostLeave(leaveUser, players);
        gameJoinService.leave(leaveUser);
        deleteRoomIfEmpty(leaveUser.getGameRoomId());
    }
    private void updateTeamColorIfNeeded(GameJoinVO existedUser, String teamColor) {
        if ((existedUser.getGameJoinTeamcolor() == null || existedUser.getGameJoinTeamcolor().isEmpty())
                && teamColor != null && !teamColor.isEmpty()) {
            existedUser.setGameJoinTeamcolor(teamColor);
            gameJoinService.updateTeamColor(existedUser);
        }
    }

    private void hostLeave(GameJoinVO leavingUser, List<GameJoinVO> players) {
        boolean isHostLeaving = leavingUser.getGameJoinIsHost() != null && leavingUser.getGameJoinIsHost() == 1;
        if (!isHostLeaving) return;

        players.stream()
                .filter(player -> !player.getUserId().equals(leavingUser.getUserId()))
                .findFirst()
                .ifPresent(nextUser -> {
                    nextUser.setGameJoinIsHost(1);
                    gameJoinService.update(nextUser);
                });
    }

    private void deleteRoomIfEmpty(Long gameRoomId) {
        List<GameJoinVO> remaining = gameJoinService.getUserListByEntrancedTime(gameRoomId);
        if (remaining.isEmpty()) {
            gameRoomService.delete(gameRoomId);
        }
    }

}
