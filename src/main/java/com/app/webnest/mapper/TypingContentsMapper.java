package com.app.webnest.mapper;

import com.app.webnest.domain.dto.TypingContentsDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TypingContentsMapper {
    //긴글연습
    public List<TypingContentsDTO> findLongContentsByLanguage(String language);

    //상세조회
    public TypingContentsDTO findContentById(Long id);

    //짧은글연습
    public List<TypingContentsDTO> findShortContentsByLanguage(String language);
}
