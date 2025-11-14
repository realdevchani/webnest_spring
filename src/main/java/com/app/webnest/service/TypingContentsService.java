package com.app.webnest.service;

import com.app.webnest.domain.dto.TypingContentsDTO;

import java.util.List;


public interface TypingContentsService {
    // 긴글 리스트 (언어별)
    List<TypingContentsDTO> getLongContentsByLanguage(String language);

    // 특정 콘텐츠 상세
    TypingContentsDTO getContent(Long id);
}
