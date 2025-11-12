package com.app.webnest.mapper;

import com.app.webnest.domain.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ChatMessageMapperTest {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Test
    void selectAll() {
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        chatMessageVO.setGameRoomId(12L);
        log.info("selectAll:{}", chatMessageMapper.selectAll(chatMessageVO));
    }
}