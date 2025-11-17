package com.app.webnest.service;

import com.app.webnest.domain.dto.TypingRecordDTO;
import com.app.webnest.domain.vo.TypingRecordVO;
import com.app.webnest.repository.TypingRecordDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TypingRecordServiceImpl implements TypingRecordService {
    private final TypingRecordDAO typingRecordDAO;

    @Override
    public void saveRecord(double wpm, double accuracy, double time, Long userId, Long contentsId) {

        TypingRecordVO vo = new TypingRecordVO();

        vo.setTypingRecordTypist(wpm);
        vo.setTypingRecordAccuracy(accuracy);
        vo.setTypingRecordTime(time);
        vo.setUserId(userId);
        vo.setTypingContentsId(contentsId);

        typingRecordDAO.save(vo);
    }
    @Override
    public List<TypingRecordDTO> getUserRecords(Long userId) {
        return typingRecordDAO.getRecordsByUser(userId);
    }
}
