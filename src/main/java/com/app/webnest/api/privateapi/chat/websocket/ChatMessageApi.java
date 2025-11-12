package com.app.webnest.api.privateapi.chat.websocket;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.service.ChatMessageService;
import com.app.webnest.service.GameJoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatMessageApi {

    private final ChatMessageService chatMessageService;
    private final GameJoinService gameJoinService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chats/send")
    public void sendMessage(ChatMessageVO chatMessageVO) {

        ChatMessageDTO chatMessageDTO = null;
        GameJoinVO gameJoinVO = new GameJoinVO(chatMessageVO);
        String type = chatMessageVO.getChatMessageType();

        if(type.equals("JOIN")){
            gameJoinService.join(gameJoinVO);
        }else if(type.equals("LEAVE")){
            gameJoinService.leave(gameJoinVO);
        }else if(type.equals("MESSAGE")){
            chatMessageService.sendChat(chatMessageVO);
        }

        chatMessageDTO = chatMessageService.getChatByRoomId(chatMessageVO);

        // 브로드 캐스트
        if(chatMessageDTO != null){
            if(chatMessageVO.getUserReceiverId() == null){
                // receiver가 없을 때, 방 전체 전송
                simpMessagingTemplate.convertAndSend(
                        "/sub/chats/room/" + chatMessageVO.getGameRoomId(),
                        chatMessageDTO
                );

            }else{
                // 1:1 메세지
                simpMessagingTemplate.convertAndSend(
                        "/sub/chats/room/" + chatMessageVO.getGameRoomId() + "/" + chatMessageVO.getUserReceiverId(),
                        chatMessageDTO
                );
            }
        }
    }


}
