package com.app.webnest.api.privateapi.chat.websocket;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.exception.GameJoinException;
import com.app.webnest.service.ChatMessageService;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.GameRoomLifecycleService;
import com.app.webnest.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
    private final GameRoomLifecycleService gameRoomLifecycleService;

    @MessageMapping("/chats/send")
    public void sendMessage(ChatMessageVO chatMessageVO, SimpMessageHeaderAccessor headerAccessor) {
        // 유효성 검증 (유지)
        if (chatMessageVO.getUserSenderId() == null || chatMessageVO.getUserSenderId() <= 0) return;
        Long gameRoomId = chatMessageVO.getGameRoomId();
        if (gameRoomId == null || gameRoomId <= 0) return;

        String type = chatMessageVO.getChatMessageType();
        String sessionId = headerAccessor.getSessionId();

        switch (type) {
            case "JOIN" -> {
                gameRoomLifecycleService.handleJoin(chatMessageVO);
                gameRoomLifecycleService.registerSession(sessionId, chatMessageVO.getUserSenderId(), gameRoomId);
            }
            case "LEAVE" -> {
                gameRoomLifecycleService.handleLeave(chatMessageVO);
                gameRoomLifecycleService.unregisterSession(sessionId);
            }
            case "MESSAGE" -> handleChatMessage(chatMessageVO);
        }
    }

    private void handleChatMessage(ChatMessageVO chatMessageVO) {
        GameJoinVO gameJoinVO = new GameJoinVO(chatMessageVO);
        Optional<GameJoinVO> existingGameJoin = gameJoinService.getGameJoinDTOByGameRoomId(gameJoinVO);

        if (existingGameJoin.isPresent()) {
            // 팀컬러 싱크는 lifecycleService의 헬퍼와 동일하지만
            // MESSAGE에서는 직접 처리 (lifecycle 범위 밖)
            GameJoinVO existing = existingGameJoin.get();
            if ((existing.getGameJoinTeamcolor() == null || existing.getGameJoinTeamcolor().isEmpty())
                    && chatMessageVO.getUserSenderTeamcolor() != null
                    && !chatMessageVO.getUserSenderTeamcolor().isEmpty()) {
                existing.setGameJoinTeamcolor(chatMessageVO.getUserSenderTeamcolor());
                gameJoinService.updateTeamColor(existing);
            }

            chatMessageService.sendChat(chatMessageVO);

            if (chatMessageVO.getId() != null && chatMessageVO.getId() > 0) {
                ChatMessageDTO chatMessageDTO = chatMessageService.getChatByRoomId(chatMessageVO);
                if (chatMessageDTO != null) {
                    broadcastChat(chatMessageVO, chatMessageDTO);
                }
            }
        }
    }

    private void broadcastChat(ChatMessageVO vo, ChatMessageDTO dto) {
        String destination = "/sub/chats/room/" + vo.getGameRoomId();
        if (vo.getUserReceiverId() != null) {
            destination += "/" + vo.getUserReceiverId();
        }
        simpMessagingTemplate.convertAndSend(destination, dto);
    }
}