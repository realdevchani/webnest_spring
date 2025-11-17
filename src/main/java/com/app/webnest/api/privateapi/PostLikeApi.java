package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.vo.PostLikeVO;
import com.app.webnest.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/postLike")
public class PostLikeApi {

    private final PostLikeService postLikeService;

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponseDTO> getPostLikeCount(@PathVariable("postId") Long postId) {
        int likeCount = postLikeService.getPostLike(postId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("게시글 좋아요 수 조회 성공", likeCount));
    }

    @PostMapping("/postlike")
    public ResponseEntity<ApiResponseDTO> writePostLike(@RequestBody PostLikeVO postLikeVO) {
        Map<String, Long> response = postLikeService.save(postLikeVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.of("게시글 좋아요 완료", response));
    }

//    @DeleteMapping("/delete")
//    public ResponseEntity<ApiResponseDTO> deletePostLike(@RequestBody Long id) {
//        postLikeService.deletePostLike(id);
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 좋아요 삭제 성공"));
//    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponseDTO> deletePostLike(@RequestBody PostLikeVO postLikeVO) {
        postLikeService.deleteByUserAndPost(postLikeVO);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 좋아요 삭제 성공"));
    }

}

