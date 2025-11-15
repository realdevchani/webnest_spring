package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.vo.CommentVO;
import com.app.webnest.domain.vo.SubcommentLikeVO;
import com.app.webnest.service.CommentLikeService;
import com.app.webnest.service.SubcommentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/subcommentLike")
public class SubcommentLikeApi {

    private final SubcommentLikeService subcommentLikeService;

    @GetMapping("/{subcommentId}")
    public ResponseEntity<ApiResponseDTO> getSubcommentLikeCount(@PathVariable("subcommentId") Long subcommentId) {
        int likeCount = subcommentLikeService.getSubcommentLike(subcommentId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("댓글 좋아요 수 조회 성공", likeCount));
    }

    @PostMapping("/subcommentlike")
    public ResponseEntity<ApiResponseDTO> writeComments(@RequestBody SubcommentLikeVO subcommentLikeVO) {
        Map<String, Long> response = subcommentLikeService.save(subcommentLikeVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.of("대댓글 좋아요 완료", response));
    }

//    @DeleteMapping("/delete")
//    public ResponseEntity<ApiResponseDTO> updatePost(@RequestBody Long id) {
//        subcommentLikeService.deleteSubcommentLike(id);
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 삭제 성공"));
//    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponseDTO> deletePost(@RequestBody SubcommentLikeVO subcommentLikeVO) {
        subcommentLikeService.deleteByUserAndSubcomment(subcommentLikeVO);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("대댓글 좋아요 삭제 성공"));
    }

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponseDTO> toggle(
            @RequestParam Long subcommentId,
            @RequestParam Long userId
    ) {
        Map<String, Object> result = subcommentLikeService.toggleLike(subcommentId, userId);
        return ResponseEntity.ok(ApiResponseDTO.of("대댓글 좋아요 토글 성공", result));
    }

}
