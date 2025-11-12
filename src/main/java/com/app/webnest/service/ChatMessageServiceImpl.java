package com.app.webnest.service;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import com.app.webnest.mapper.ChatMessageMapper;
import com.app.webnest.repository.ChatMessageDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageDAO chatMessageDAO;

    @Override
    public void sendChat(ChatMessageVO chatMessageVO) {
        chatMessageDAO.save(chatMessageVO);
    }

    @Override
    public List<ChatMessageDTO> getChatListByRoomId(ChatMessageVO chatMessageVO) {
        return chatMessageDAO.findAll(chatMessageVO);
    }

    @Override
    public ChatMessageDTO getChatByRoomId(ChatMessageVO chatMessageVO) {
        return chatMessageDAO.find(chatMessageVO);
    }

    @Override
    public void updateReadStatus(ChatMessageVO chatMessageVO) {
        chatMessageDAO.updateReadStatus(chatMessageVO);
    }
}
