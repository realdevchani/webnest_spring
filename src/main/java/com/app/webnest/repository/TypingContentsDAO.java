package com.app.webnest.repository;

import com.app.webnest.domain.dto.TypingContentsDTO;
import com.app.webnest.mapper.TypingContentsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TypingContentsDAO {
    private final TypingContentsMapper typingContentsMapper;

    // 언어별 긴글 리스트 가져오기
    public List<TypingContentsDTO> findLongContentsByLanguage(String language) {
        return typingContentsMapper.findLongContentsByLanguage(language);
    }

    // ID로 긴글 상세 조회
    public TypingContentsDTO findById(Long id) {
        return typingContentsMapper.findContentById(id);
    }
}
