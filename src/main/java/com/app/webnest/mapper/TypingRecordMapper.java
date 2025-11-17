package com.app.webnest.mapper;

import com.app.webnest.domain.dto.TypingRecordDTO;
import com.app.webnest.domain.vo.TypingRecordVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TypingRecordMapper {
    public void insert(TypingRecordVO typingRecordVO);

    public List<TypingRecordDTO> findByUserId(Long userId);
}
