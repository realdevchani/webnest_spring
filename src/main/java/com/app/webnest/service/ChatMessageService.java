package com.app.webnest.service;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.vo.ChatMessageVO;

import java.util.List;

public interface ChatMessageService {

    // 채팅 전송 및 추가
    public void sendChat(ChatMessageVO chatMessageVO);

    // 해당 채팅방에 메세지 전체 조회 (1:1 포함)
    public List<ChatMessageDTO> getChatListByRoomId(ChatMessageVO chatMessageVO);

    public ChatMessageDTO getChatByRoomId(ChatMessageVO chatMessageVO);

    // 특정 사용자 읽음 처리
    public void updateReadStatus(ChatMessageVO chatMessageVO);
}
