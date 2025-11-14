package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.service.TypingContentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/typing")
public class TypingContentsApi {
    private final TypingContentsService typingContentsService;

    @GetMapping("/long/list")
    public ApiResponseDTO getLongList(@RequestParam String language) {
        return ApiResponseDTO.of(
                "긴글 리스트 조회 성공",
                typingContentsService.getLongContentsByLanguage(language)
        );
    }

    @GetMapping("/long/{id}")
    public ApiResponseDTO getContent(@PathVariable Long id) {
        return ApiResponseDTO.of(
                "긴글 상세 조회 성공",
                typingContentsService.getContent(id)
        );
    }

}


