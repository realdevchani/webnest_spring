package com.app.webnest.api.privateapi;


import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.CommentDTO;
import com.app.webnest.domain.dto.PostResponseDTO;
import com.app.webnest.domain.vo.CommentNotificationVO;
import com.app.webnest.domain.vo.CommentVO;
import com.app.webnest.domain.vo.PostVO;
import com.app.webnest.service.CommentService;
import com.app.webnest.service.NotificationService;
import com.app.webnest.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentApi {
    private final CommentService commentService;
    private final NotificationService notificationService;
    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponseDTO> getPost(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 조회 성공", comments));
    }


        //답글 작성
    @PostMapping("/write")
    public ResponseEntity<ApiResponseDTO> writeComments(@RequestBody CommentVO commentVO) {
        Map<String, Long> response = commentService.writeComment(commentVO);
        
        // 댓글 알람 추가
        try {
            // 게시글 작성자 조회
            PostResponseDTO post = postService.getPostWithoutView(commentVO.getPostId(), null);
            Long postAuthorId = post.getUserId(); // 게시글 작성자
            
            // 자기 자신에게는 알람을 보내지 않음
            if (!commentVO.getUserId().equals(postAuthorId)) {
                CommentNotificationVO commentNotificationVO = new CommentNotificationVO();
                commentNotificationVO.setActorUserId(commentVO.getUserId()); // 댓글 작성자
                commentNotificationVO.setReceiverUserId(postAuthorId); // 게시글 작성자
                commentNotificationVO.setCommentId(response.get("newCommentId")); // 생성된 댓글 ID
                commentNotificationVO.setCommentNotificationAction("COMMENT"); // 댓글 액션
                commentNotificationVO.setCommentNotificationIsRead(false); // 읽지 않음
                commentNotificationVO.setNotificationCreateAt(new Date());
                
                notificationService.addCommentNotification(commentNotificationVO);
                log.info("✅ 댓글 알람 추가 완료 - actorUserId: {}, receiverUserId: {}, commentId: {}, postId: {}", 
                        commentNotificationVO.getActorUserId(), commentNotificationVO.getReceiverUserId(), 
                        commentNotificationVO.getCommentId(), commentVO.getPostId());
            } else {
                log.info("ℹ️ 자기 자신의 게시글에 댓글 작성 - 알람 미발송. userId: {}, postId: {}", 
                        commentVO.getUserId(), commentVO.getPostId());
            }
        } catch (Exception e) {
            log.error("❌ 댓글 알람 추가 실패 - error: {}", e.getMessage(), e);
            // 알람 추가 실패해도 댓글 작성은 성공한 것으로 처리
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.of("게시글 작성 완료", response));
    }

    //답글 수정
    @PutMapping("modify")
    public ResponseEntity<ApiResponseDTO> updateComment(@RequestBody CommentVO commentVO) {
        commentService.modifyComment(commentVO);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("수정 조회 성공"));
    }

    //답글 삭제
    @DeleteMapping("remove")
    public ResponseEntity<ApiResponseDTO> deleteComment(@RequestBody Long id) {
        commentService.removeComment(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 삭제 성공"));
    }


    //채택
    @PostMapping("/choose")
    public ResponseEntity<?> chooseComment(@RequestBody Map<String, Long> data) {
        Long commentId = data.get("commentId");

        commentService.chooseComment(commentId);

        return ResponseEntity.ok(Map.of(
                "success", true
        ));
    }

}


