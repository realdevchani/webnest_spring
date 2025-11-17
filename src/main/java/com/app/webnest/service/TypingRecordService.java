package com.app.webnest.service;

import com.app.webnest.domain.dto.TypingRecordDTO;
import com.app.webnest.domain.vo.TypingRecordVO;

import java.util.List;

public interface TypingRecordService {
    public void saveRecord(double wpm, double accuracy, double time, Long userId, Long contentsId);

    public List<TypingRecordDTO> getUserRecords(Long userId);
}
