package com.app.webnest.service;

import com.app.webnest.domain.dto.TypingContentsDTO;
import com.app.webnest.exception.TypingException;
import com.app.webnest.repository.TypingContentsDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class TypingContentsServiceImpl implements TypingContentsService {
    private final TypingContentsDAO typingContentsDAO;

    // 긴글 언어별 리스트
    @Override
    public List<TypingContentsDTO> getLongContentsByLanguage(String language) {
        return typingContentsDAO.findLongContentsByLanguage(language);
    }

    // 상세 조회
    @Override
    public TypingContentsDTO getContent(Long id) {
        TypingContentsDTO content = typingContentsDAO.findById(id);

        if (content == null) {
            throw new TypingException("해당 콘텐츠를 찾을 수 없습니다. ID=" + id);
        }

        return content;
    }

    //짧은글 연습
    // 언어별 짧은글 리스트
    public List<TypingContentsDTO> findShortContentsByLanguage(String language) {
        return typingContentsDAO.findShortContentsByLanguage(language);
    }
}
