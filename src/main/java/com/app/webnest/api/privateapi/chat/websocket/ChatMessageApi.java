package com.app.webnest.api.privateapi.chat.websocket;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.exception.GameJoinException;
import com.app.webnest.service.ChatMessageService;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ChatMessageApi {

    private final ChatMessageService chatMessageService;
    private final GameJoinService gameJoinService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final GameRoomService gameRoomService;

    @MessageMapping("/chats/send")
    public void sendMessage(ChatMessageVO chatMessageVO) {
        ChatMessageDTO chatMessageDTO = null;
        String type = chatMessageVO.getChatMessageType();

        // 유효하지 않은 userSenderId 체크
        if(chatMessageVO.getUserSenderId() == null || chatMessageVO.getUserSenderId() <= 0) {
            return;
        }

        // 게임방 ID 유효성 검증
        Long gameRoomId = chatMessageVO.getGameRoomId();
        if (gameRoomId == null || gameRoomId <= 0) {
            return;
        }

        GameJoinVO gameJoinVO = new GameJoinVO(chatMessageVO);
        Optional<GameJoinVO> existingGameJoin = gameJoinService.getGameJoinDTOByGameRoomId(gameJoinVO);
        boolean alreadyExistUserInRoom = existingGameJoin.isPresent();

        if(type.equals("JOIN")){
            // 게임방 존재 여부 확인
            try {
                gameRoomService.getRoom(gameRoomId);
            } catch (Exception e) {
                // 게임방이 없어도 JOIN은 진행 (이미 방에 참여한 사용자는 채팅 가능)
            }
            
            if(!alreadyExistUserInRoom){
                if(gameJoinVO.getGameJoinIsHost() == null) {
                    gameJoinVO.setGameJoinIsHost(0);
                }
                gameJoinService.join(gameJoinVO);
                
                // 호스트가 없는지 확인하고 자동으로 호스트 지정
                List<GameJoinDTO> allPlayers = gameJoinService.getPlayers(gameRoomId);
                boolean hasHost = allPlayers.stream()
                        .anyMatch(player -> player.isGameJoinIsHost());
                
                if (!hasHost && !allPlayers.isEmpty()) {
                    GameJoinDTO firstPlayer = allPlayers.get(0);
                    GameJoinVO hostVO = new GameJoinVO();
                    hostVO.setUserId(firstPlayer.getUserId());
                    hostVO.setGameRoomId(gameRoomId);
                    hostVO.setGameJoinIsHost(1);
                    gameJoinService.update(hostVO);
                }
            } else {
                // 이미 존재하는 경우 - 호스트 정보는 절대 변경하지 않음
                GameJoinVO existing = existingGameJoin.get();
                
                // 호스트 정보 보호: DB의 호스트 정보를 유지 (프론트에서 보낸 값으로 덮어쓰지 않음)
                // 팀 컬러만 업데이트 (호스트 정보는 변경하지 않음)
                if((existing.getGameJoinTeamcolor() == null || existing.getGameJoinTeamcolor().isEmpty()) 
                   && chatMessageVO.getUserSenderTeamcolor() != null 
                   && !chatMessageVO.getUserSenderTeamcolor().isEmpty()) {
                    existing.setGameJoinTeamcolor(chatMessageVO.getUserSenderTeamcolor());
                    gameJoinService.updateTeamColor(existing);
                }
            }
        } else if(type.equals("LEAVE")) {
            // 현재 사용자 정보 조회
            Optional<GameJoinVO> currentUserOpt = gameJoinService.getGameJoinDTOByGameRoomId(gameJoinVO);
            if (!currentUserOpt.isPresent()) {
                return;
            }
            
            GameJoinVO currentUser = currentUserOpt.get();
            Long currentRoomId = gameJoinVO.getGameRoomId();
            
            // 현재 방의 모든 플레이어 조회
            List<GameJoinVO> foundPlayers = gameJoinService.getUserListByEntrancedTime(currentRoomId);
            
            // 혼자 남은 유저가 나간 경우 -> 방 폭파
            if (foundPlayers.size() <= 1) {
                gameJoinService.leave(gameJoinVO);
                gameRoomService.delete(currentRoomId);
                return;
            }
            
            // 호스트가 나가는 경우에만 호스트 전환
            boolean isHostLeaving = currentUser.getGameJoinIsHost() != null && currentUser.getGameJoinIsHost() == 1;
            if (isHostLeaving) {
                // 현재 사용자의 인덱스 찾기 (userId로 비교)
                int currentUserIndex = -1;
                for (int i = 0; i < foundPlayers.size(); i++) {
                    if (foundPlayers.get(i).getUserId().equals(currentUser.getUserId())) {
                        currentUserIndex = i;
                        break;
                    }
                }
                
                if (currentUserIndex == -1) {
                    gameJoinService.leave(gameJoinVO);
                    return;
                }
                
                // 다음 유저 찾기 (현재 유저 제외)
                GameJoinVO nextUser = null;
                for (int i = 0; i < foundPlayers.size(); i++) {
                    if (i != currentUserIndex) {
                        nextUser = foundPlayers.get(i);
                        break;
                    }
                }
                
                if (nextUser != null) {
                    nextUser.setGameJoinIsHost(1);
                    gameJoinService.update(nextUser);
                }
            }
            
            // 사용자 퇴장 처리
            gameJoinService.leave(gameJoinVO);
            
            // 퇴장 후 남은 플레이어 수 확인
            List<GameJoinVO> remainingPlayers = gameJoinService.getUserListByEntrancedTime(currentRoomId);
            if (remainingPlayers.isEmpty()) {
                gameRoomService.delete(currentRoomId);
            }
        } else if (type.equals("MESSAGE")) {
            // 게임방 존재 여부 확인 (채팅 전송을 막지 않음)
            try {
                gameRoomService.getRoom(gameRoomId);
            } catch (Exception e) {
                // 게임방이 없어도 채팅은 전송 가능하도록 계속 진행
            }

            // MESSAGE 전에 TBL_GAME_JOIN에 팀 컬러가 있는지 확인
            if (alreadyExistUserInRoom) {
                GameJoinVO existing = existingGameJoin.get();
                // 팀 컬러가 없고 프론트에서 보낸 팀 컬러가 있으면 업데이트
                if ((existing.getGameJoinTeamcolor() == null || existing.getGameJoinTeamcolor().isEmpty())
                        && chatMessageVO.getUserSenderTeamcolor() != null
                        && !chatMessageVO.getUserSenderTeamcolor().isEmpty()) {
                    existing.setGameJoinTeamcolor(chatMessageVO.getUserSenderTeamcolor());
                    gameJoinService.updateTeamColor(existing);
                }

                chatMessageService.sendChat(chatMessageVO);
                
                // sendChat 후 생성된 id를 사용하여 조회
                // 프론트에서 id를 보내지 않아도 됨 (백엔드에서 자동 생성)
                // id가 0이 아닌 유효한 값일 때만 조회
                if (chatMessageVO.getId() != null && chatMessageVO.getId() > 0) {
                    chatMessageDTO = chatMessageService.getChatByRoomId(chatMessageVO);
                } else {
                    // id가 생성되지 않았으면 조회하지 않음
                    return;
                }

                // 브로드 캐스트
                if (chatMessageDTO != null) {
                    if (chatMessageVO.getUserReceiverId() == null) {
                        // receiver가 없을 때, 방 전체 전송
                        simpMessagingTemplate.convertAndSend(
                                "/sub/chats/room/" + chatMessageVO.getGameRoomId(),
                                chatMessageDTO
                        );
                    } else {
                        // 1:1 메세지
                        simpMessagingTemplate.convertAndSend(
                                "/sub/chats/room/" + chatMessageVO.getGameRoomId() + "/" + chatMessageVO.getUserReceiverId(),
                                chatMessageDTO
                        );
                    }
                }
            }
        }
    }
}