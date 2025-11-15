package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.CommentLikeDTO;
import com.app.webnest.domain.vo.CommentLikeVO;
import com.app.webnest.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/commentLike")
public class CommentLikeApi {

    private final CommentLikeService commentLikeService;

    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponseDTO> getCommentLikeCount(@PathVariable("commentId") Long commentId) {
        int likeCount = commentLikeService.getCommentLike(commentId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("댓글 좋아요 수 조회 성공", likeCount));
    }

    @PostMapping("/commentlike")
    public ResponseEntity<ApiResponseDTO> writeCommentLike(@RequestBody CommentLikeDTO commentLikeDTO) {
        Map<String, Long> response = commentLikeService.save(commentLikeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.of("댓글 좋아요 완료", response));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDTO> deleteCommentLike(@RequestBody Long id) {
        commentLikeService.deleteCommentLike(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("댓글 좋아요 삭제 성공"));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponseDTO> deleteCommentLikeByVO(@RequestBody CommentLikeVO commentLikeVO) {
        commentLikeService.deleteByUserAndComment(commentLikeVO);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("댓글 좋아요 삭제 성공"));
    }

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponseDTO> toggle(
            @RequestParam Long commentId,
            @RequestParam Long postId,
            @RequestParam Long userId
    ) {
        Map<String, Object> result = commentLikeService.toggleLike(commentId, postId, userId);
        return ResponseEntity.ok(ApiResponseDTO.of("댓글 좋아요 토글 성공", result));
    }
}
