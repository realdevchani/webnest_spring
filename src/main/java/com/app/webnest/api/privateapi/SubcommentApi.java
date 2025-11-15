package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.SubcommentDTO;
import com.app.webnest.domain.vo.SubcommentVO;
import com.app.webnest.service.SubcommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/subcomment")
public class SubcommentApi {
    private final SubcommentService subcommentService;

    // GET /subcomment/get-comments/{commentId}
    @GetMapping("/get-comments/{commentId}")
    public ResponseEntity<ApiResponseDTO> getSubcomments(@PathVariable("commentId") Long commentId) {
        List<SubcommentDTO> subcomments = subcommentService.getSubcomments(commentId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("대댓글 조회 성공", subcomments));
    }

    // 대댓글 작성
    @PostMapping("/write")
    public ResponseEntity<ApiResponseDTO> writeSubcomment(@RequestBody SubcommentVO subcommentVO) {
        Map<String, Long> response = subcommentService.writeSubcomment(subcommentVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.of("대댓글 작성 완료", response));
    }

    // 대댓글 삭제
    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponseDTO> deleteSubcomment(@RequestBody Long id) {
        subcommentService.removeSubcomment(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("대댓글 삭제 성공"));
    }
}
