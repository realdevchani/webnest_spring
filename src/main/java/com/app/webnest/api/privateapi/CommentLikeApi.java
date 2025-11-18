package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.CommentDTO;
import com.app.webnest.domain.dto.CommentLikeDTO;
import com.app.webnest.domain.vo.CommentLikeVO;
import com.app.webnest.domain.vo.CommentNotificationVO;
import com.app.webnest.service.CommentLikeService;
import com.app.webnest.service.CommentService;
import com.app.webnest.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/commentLike")
public class CommentLikeApi {

    private final CommentLikeService commentLikeService;
    private final NotificationService notificationService;
    private final CommentService commentService;

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
        Boolean isLiked = (Boolean) result.get("liked");
        
        // 좋아요가 추가된 경우에만 알람 전송 (좋아요 취소 시에는 알람 없음)
        // toggleLike의 결과가 liked=true면 좋아요 추가, false면 좋아요 취소
        if (isLiked != null && isLiked) {
            try {
                // 댓글 작성자 조회
                CommentDTO comment = commentService.getCommentById(commentId);
                Long commentAuthorId = comment.getUserId(); // 댓글 작성자
                
                // 자기 자신에게는 알람을 보내지 않음
                if (!userId.equals(commentAuthorId)) {
                    CommentNotificationVO commentNotificationVO = new CommentNotificationVO();
                    commentNotificationVO.setActorUserId(userId); // 좋아요 누른 사람
                    commentNotificationVO.setReceiverUserId(commentAuthorId); // 댓글 작성자
                    commentNotificationVO.setCommentId(commentId); // 댓글 ID
                    commentNotificationVO.setCommentNotificationAction("New Like"); // 좋아요 액션
                    commentNotificationVO.setCommentNotificationIsRead(false); // 읽지 않음
                    commentNotificationVO.setNotificationCreateAt(new Date());
                    
                    notificationService.addCommentNotification(commentNotificationVO);
                    log.info("✅ 댓글 좋아요 알람 추가 완료 - actorUserId: {}, receiverUserId: {}, commentId: {}", 
                            commentNotificationVO.getActorUserId(), commentNotificationVO.getReceiverUserId(), 
                            commentNotificationVO.getCommentId());
                } else {
                    log.info("ℹ️ 자기 자신의 댓글에 좋아요 - 알람 미발송. userId: {}, commentId: {}", 
                            userId, commentId);
                }
            } catch (Exception e) {
                log.error("❌ 댓글 좋아요 알람 추가 실패 - error: {}", e.getMessage(), e);
                // 알람 추가 실패해도 좋아요는 성공한 것으로 처리
            }
        }
        
        return ResponseEntity.ok(ApiResponseDTO.of("댓글 좋아요 토글 성공", result));
    }
}
