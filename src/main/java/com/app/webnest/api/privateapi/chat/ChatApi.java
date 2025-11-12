package com.app.webnest.api.privateapi.chat;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import com.app.webnest.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats/*")
@RequiredArgsConstructor
public class ChatApi {
    private final ChatMessageService chatMessageService;

    // 채팅방 메시지 불러오기
    @GetMapping("/get-messages/{gameRoomId}")
    public List<ChatMessageDTO> getChats(
            @PathVariable("gameRoomId") Long gameRoomId,
            @RequestParam("userSenderId") Long userSenderId,
            @RequestParam(value = "userReceiverId", required = false) Long userReceiverId
    ) {
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        chatMessageVO.setGameRoomId(gameRoomId);
        chatMessageVO.setUserSenderId(userSenderId);
        chatMessageVO.setUserReceiverId(userReceiverId);
        return chatMessageService.getChatListByRoomId(chatMessageVO);
    }

    // 읽음 처리
    @PatchMapping("/read/{myChatRoomId}/{customerReceiverId}")
    public void markAsRead(@PathVariable Long myChatRoomId, @PathVariable Long customerReceiverId) {
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        chatMessageVO.setGameRoomId(myChatRoomId);
        chatMessageVO.setUserReceiverId(customerReceiverId);
        chatMessageService.updateReadStatus(chatMessageVO);
    }
}
