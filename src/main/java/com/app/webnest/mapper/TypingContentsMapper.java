package com.app.webnest.mapper;

import com.app.webnest.domain.dto.TypingContentsDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TypingContentsMapper {
    public List<TypingContentsDTO> findLongContentsByLanguage(String language);

    public TypingContentsDTO findContentById(Long id);
}
