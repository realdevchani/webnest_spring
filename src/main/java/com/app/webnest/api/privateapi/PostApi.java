package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.PostResponseDTO;
import com.app.webnest.domain.vo.PostNotificationVO;
import com.app.webnest.domain.vo.PostVO;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostApi {

    private final PostService postService;
    private final NotificationService notificationService;

//    @PostMapping("write")
//    public ResponseEntity<ApiResponseDTO> writePost(@RequestBody PostVO postVO) {
//        Map<String, Long> response = postService.write(postVO);
//        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.of("게시글 작성 완료", response));
//    }

    // 열린둥지 전체 조회
    @GetMapping("/open")
    public List<PostResponseDTO> getOpenPosts() {
        return postService.getOpenPosts();
    }


    // 문제둥지 전체 조회
    @GetMapping("/question")
    public List<PostResponseDTO> getQuestionPosts() {
        List<PostResponseDTO> posts = postService.getQuestionPosts(); // ✅ 리스트 선언
        System.out.println("🔥 게시글 개수: " + posts.size()); // ✅ size() 찍기
        return posts; // ✅ 그대로 반환
    }


//    // 상세 조회
//    @GetMapping("get-post/{id}")
//    public ResponseEntity<ApiResponseDTO> getPost(@PathVariable Long id) {
//        PostResponseDTO post = postService.getPost(id);
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 조회 성공", post));
//    }
//
//    //조회수 증가 안됨
//    @GetMapping("/get-post-no-view/{id}")
//    public ResponseEntity<ApiResponseDTO> getPostNoView(@PathVariable Long id) {
//        PostResponseDTO post = postService.getPostWithoutView(id);
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ApiResponseDTO.of("조회수 증가 없이 조회", post));
//    }
    // 조회수 증가 O
    @GetMapping("get-post/{id}")
    public ResponseEntity<ApiResponseDTO> getPost(
            @PathVariable Long id,
            @RequestParam Long userId
    ){
        PostResponseDTO post = postService.getPost(id, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.of("게시글 조회 성공", post));
    }

    // 조회수 증가 X
    @GetMapping("/get-post-no-view/{id}")
    public ResponseEntity<ApiResponseDTO> getPostNoView(
            @PathVariable Long id,
            @RequestParam Long userId
    ){
        PostResponseDTO post = postService.getPostWithoutView(id, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.of("조회수 증가 없이 조회", post));
    }

//    // 마이페이지 - 열린둥지 전체
//    @GetMapping("/users/{userId}/open")
//    public List<PostResponseDTO> getMyOpenPosts(@PathVariable Long userId){
//        return postService.getOpenPostsByUserId(userId);
//    }
//
//    // 마이페이지 - 문제둥지 전체
//    @GetMapping("/users/{userId}/question")
//    public List<PostResponseDTO> getMyQuestionPosts(@PathVariable Long userId){
//        return postService.getQuestionPostsByUserId(userId);
//    }

//    @PutMapping("modify")
//    public ResponseEntity<ApiResponseDTO> updatePost(@RequestBody PostVO postVO) {
//        postService.modify(postVO);
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 수정 조회 성공"));
//    }
//
//    @DeleteMapping("remove")
//    public ResponseEntity<ApiResponseDTO> updatePost(@RequestBody Long id) {
//        postService.remove(id);
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게시글 삭제 성공"));
//    }

    //게시글 작성
    @PostMapping("/write")
    public ResponseEntity<ApiResponseDTO> writePost(@RequestBody PostVO postVO) {
        Map<String, Long> response = postService.write(postVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.of("게시글 작성 완료", response));
    }




    @PostMapping("/like")
    public ResponseEntity<ApiResponseDTO> toggleLike(
            @RequestParam Long postId,
            @RequestParam Long userId
    ) {
        Map<String, Object> result = postService.togglePostLike(postId, userId);
        Boolean isLiked = (Boolean) result.get("liked");
        
        // 좋아요가 추가된 경우에만 알람 전송 (좋아요 취소 시에는 알람 없음)
        if (isLiked != null && isLiked) {
            try {
                // 게시글 작성자 조회
                PostResponseDTO post = postService.getPostWithoutView(postId, null);
                Long postAuthorId = post.getUserId(); // 게시글 작성자
                
                // 자기 자신에게는 알람을 보내지 않음
                if (!userId.equals(postAuthorId)) {
                    PostNotificationVO postNotificationVO = new PostNotificationVO();
                    postNotificationVO.setActorUserId(userId); // 좋아요 누른 사람
                    postNotificationVO.setReceiverUserId(postAuthorId); // 게시글 작성자
                    postNotificationVO.setPostId(postId); // 게시글 ID
                    postNotificationVO.setPostNotificationAction("New Like"); // 좋아요 액션
                    postNotificationVO.setPostNotificationIsRead(0); // 읽지 않음
                    postNotificationVO.setNotificationCreateAt(new Date());
                    
                    notificationService.addPostNotification(postNotificationVO);
                    log.info("✅ 게시글 좋아요 알람 추가 완료 - actorUserId: {}, receiverUserId: {}, postId: {}", 
                            postNotificationVO.getActorUserId(), postNotificationVO.getReceiverUserId(), 
                            postNotificationVO.getPostId());
                } else {
                    log.info("ℹ️ 자기 자신의 게시글에 좋아요 - 알람 미발송. userId: {}, postId: {}", 
                            userId, postId);
                }
            } catch (Exception e) {
                log.error("❌ 게시글 좋아요 알람 추가 실패 - error: {}", e.getMessage(), e);
                // 알람 추가 실패해도 좋아요는 성공한 것으로 처리
            }
        }
        
        return ResponseEntity.ok(ApiResponseDTO.of("좋아요 변경 완료", result));
    }




}
