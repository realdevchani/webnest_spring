package com.app.webnest.service;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.PostResponseDTO;
import com.app.webnest.domain.vo.PostVO;
import com.app.webnest.exception.PostException;
import com.app.webnest.repository.PostDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostDAO postDAO;

    @Override
    public PostResponseDTO getPost(Long id, Long userId) {
        postDAO.updateReadCount(id);
        PostResponseDTO post = postDAO.findPost(id)
                .orElseThrow(() -> new PostException("Post Not Found"));

        int likeCount = postDAO.getPostLikeCount(id);
        post.setPostLikeCount(likeCount);

        //  로그인 유저가 좋아요 눌렀는지 여부
        boolean liked = postDAO.isPostLiked(id, userId);
        post.setLiked(liked);   // DTO에 넣어주기

        return post;
    }

    @Override
    public PostResponseDTO getPostWithoutView(Long id, Long userId) {

        PostResponseDTO post = postDAO.findPost(id)
                .orElseThrow(() -> new PostException("Post Not Found"));

        int likeCount = postDAO.getPostLikeCount(id);
        post.setPostLikeCount(likeCount);

        boolean liked = postDAO.isPostLiked(id, userId);
        post.setLiked(liked);

        return post;
    }




    @Override
    public List<PostResponseDTO> getOpenPosts() {
        return postDAO.findOpenPosts();
    }

//    @Override
//    public List<PostResponseDTO> getQuestionPosts(){
//        return postDAO.findQuestionPosts();
//    }
    @Override
    public List<PostResponseDTO> getQuestionPosts() {
        System.out.println("🧩 getQuestionPosts() 호출됨");
        List<PostResponseDTO> result = postDAO.findQuestionPosts();
        System.out.println("🧩 DAO 결과 크기: " + result.size());
        return result;
    }

    // 마이페이지 - 열린둥지 전체
    @Override public List<PostResponseDTO> getOpenPostsByUserId(Long userId){
        return postDAO.findOpenPostsByUserId(userId);
    }
    // 마이페이지 - 문제둥지 전체
    @Override public List<PostResponseDTO> getQuestionPostsByUserId(Long userId){
        return postDAO.findQuestionPostsByUserId(userId);
    }


    //게시글 작성
    @Override
    public Map<String, Long> write(PostVO postVO) {
        Map<String, Long> response = new HashMap<>();
        Long newPostId = postDAO.savePost(postVO);
        response.put("newPostId", newPostId);
        return response;
    }






    /// //////
    @Override
    public Map<String, Object> togglePostLike(Long postId, Long userId) {

        boolean isLiked = postDAO.isPostLiked(postId, userId);

        if (isLiked) {
            postDAO.removePostLike(postId, userId);
        } else {
            postDAO.addPostLike(postId, userId);
        }

        int likeCount = postDAO.getPostLikeCount(postId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", !isLiked);
        result.put("likeCount", likeCount);

        return result;
    }

}
