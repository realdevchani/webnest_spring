package com.app.webnest.repository;

import com.app.webnest.domain.dto.TypingRecordDTO;
import com.app.webnest.domain.vo.TypingRecordVO;
import com.app.webnest.mapper.TypingRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TypingRecordDAO {
    private final TypingRecordMapper typingRecordMapper;

    public void save(TypingRecordVO typingRecordVO) {
        typingRecordMapper.insert(typingRecordVO);
    }

    public List<TypingRecordDTO> getRecordsByUser(Long userId) {
        return typingRecordMapper.findByUserId(userId);
    }
}
