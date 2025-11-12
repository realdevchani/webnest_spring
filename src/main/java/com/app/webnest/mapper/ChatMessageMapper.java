package com.app.webnest.mapper;

import com.app.webnest.domain.dto.ChatMessageDTO;
import com.app.webnest.domain.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatMessageMapper {

    // 해당 채팅방 대화글 전체 조회
    public List<ChatMessageDTO> selectAll(ChatMessageVO chatMessageVO);

    // 해당 채팅방 대화글 1개 조회
    public Optional<ChatMessageDTO> select(ChatMessageVO chatMessageVO);

    // 채팅 추가
    public void insert(ChatMessageVO chatMessageVO);

    // 읽음 상태 변경
    public void updateReadStatus(ChatMessageVO chatMessageVO);

}
