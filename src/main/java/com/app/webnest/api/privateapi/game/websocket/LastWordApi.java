package com.app.webnest.api.privateapi.game.websocket;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.dto.LastWordDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.GameRoomService;
import com.app.webnest.service.LastWordService;
import com.app.webnest.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LastWordApi {
    private final GameJoinService gameJoinService;
    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserServiceImpl userService;
    private final LastWordService lastWordService;

    @MessageMapping("/game/last-word/ready")
    public void updateReady(GameJoinVO gameJoinVO) {
        log.info("Ready status update requested. gameRoomId: {}, userId: {}, isReady: {}",
                gameJoinVO.getGameRoomId(), gameJoinVO.getUserId(), gameJoinVO.getGameJoinIsReady());

        // 준비 상태 업데이트
        gameJoinService.updateReady(gameJoinVO);

        // 게임 상태 조회
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "READY_UPDATED");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }

    @MessageMapping("/game/last-word/start")
    public void startGame(GameJoinVO gameJoinVO) {

        // 게임 시작 시 모든 플레이어를 자동으로 준비완료 상태로 변경
        List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        // 모든 플레이어를 준비완료 상태로 변경
        for (GameJoinDTO player : players) {
            GameJoinVO readyVO = new GameJoinVO();
            readyVO.setUserId(player.getUserId());
            readyVO.setGameRoomId(gameJoinVO.getGameRoomId());
            readyVO.setGameJoinIsReady(1);
            gameJoinService.updateReady(readyVO);
        }

        // 게임 시작 전 초기화 (안전장치)

        // 모든 턴을 0으로 설정
        gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId());

        // 게임 상태를 다시 조회하여 최신 상태 가져오기
        List<GameJoinDTO> currentPlayers = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        // 첫 번째 플레이어의 턴을 1로 설정 -> 방장이 턴 트루 상태 나머지는 다 false
        if (!currentPlayers.isEmpty()) {
            GameJoinVO firstPlayerVO = new GameJoinVO();
            firstPlayerVO.setUserId(currentPlayers.get(0).getUserId());
            firstPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
            gameJoinService.updateUserTurn(firstPlayerVO);
            log.info("First player turn set. userId: {}, gameRoomId: {}",
                    currentPlayers.get(0).getUserId(), gameJoinVO.getGameRoomId());
        } else {
//            여기 메세지 출력 시 게임 방 조회 잘못됨 : 게임룸에 포함된 유저 없음
            log.warn("No players found to set turn. gameRoomId: {}", gameJoinVO.getGameRoomId());
        }

        // 게임 상태 조회 (턴 설정 후)
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
        log.info("Game state after turn set. Players count: {}", gameState.size());
        gameState.forEach(p -> {
            log.info("Player in gameState - userId: {}, myTurn: {}", p.getUserId(), p.isGameJoinMyturn());
        });

//        게임 상태를 started 로 바꿈.
        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STARTED");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }
    @MessageMapping("/game/last-word/state")
    public void getGameState(GameJoinVO gameJoinVO) {
        log.info("Game state requested. gameRoomId: {}", gameJoinVO.getGameRoomId());

        // 게임 상태 조회
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STATE");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }

    @MessageMapping("/game/last-word/submit")
    public void submitWord(Map<String, Object> request) {
        log.info("단어 제출 요청 수신 - request: {}", request);

        try {
            // 요청 데이터 추출
            Long userId = request.get("userId") != null ? 
                    Long.valueOf(request.get("userId").toString()) : null;
            Long gameRoomId = request.get("gameRoomId") != null ? 
                    Long.valueOf(request.get("gameRoomId").toString()) : null;
            String word = request.get("word") != null ? 
                    request.get("word").toString() : null;

            if (gameRoomId == null || word == null) {
                log.warn("필수 파라미터가 없습니다. gameRoomId: {}, word: {}", gameRoomId, word);
                return;
            }

            // LastWordDTO 생성
            LastWordDTO lastWordDTO = new LastWordDTO();
            lastWordDTO.setWord(word);
            lastWordDTO.setGameRoomId(gameRoomId);
            // explanation, color, isFocus는 프론트에서 보내지 않으면 null 또는 기본값

            // 단어 검증 및 브로드캐스트
            lastWordService.broadcastWord(lastWordDTO);

            log.info("단어 제출 처리 완료 - userId: {}, gameRoomId: {}, word: {}", 
                    userId, gameRoomId, word);

        } catch (Exception e) {
            log.error("단어 제출 처리 중 오류 발생 - request: {}, error: {}", request, e.getMessage(), e);
        }
    }
}
