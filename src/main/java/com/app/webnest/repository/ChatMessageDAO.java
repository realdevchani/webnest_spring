package com.app.webnest.repository;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import com.app.webnest.exception.ChatException;
import com.app.webnest.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageDAO {

    private final ChatMessageMapper chatMessageMapper;

    // 채팅 추가
    public void save(ChatMessageVO chatMessageVO){
        chatMessageMapper.insert(chatMessageVO);
    }

    // 해당 채팅방에 메세지 전체 조회
    public List<ChatMessageDTO> findAll(ChatMessageVO chatMessageVO){
        return chatMessageMapper.selectAll(chatMessageVO);
    }

    public ChatMessageDTO find(ChatMessageVO chatMessageVO){
        return chatMessageMapper.select(chatMessageVO).orElse(null);
    }

    // 특정 사용자 읽음 처리
    public void updateReadStatus(ChatMessageVO chatMessageVO){
        chatMessageMapper.updateReadStatus(chatMessageVO);
    }
}
