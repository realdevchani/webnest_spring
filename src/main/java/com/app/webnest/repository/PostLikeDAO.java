package com.app.webnest.repository;

import com.app.webnest.domain.dto.PostLikeDTO;
import com.app.webnest.domain.vo.PostLikeVO;
import com.app.webnest.mapper.PostLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostLikeDAO {
    private final PostLikeMapper postLikeMapper;

    public Long save(PostLikeVO postLikeVO) {
        postLikeMapper.insert(postLikeVO);
        return postLikeVO.getId();
    }


    public int findPostLike(Long postId) {
        return postLikeMapper.selectByPostIdcount(postId);
    }

    public void remove(Long id) {
        postLikeMapper.delete(id);
    }

    public void remove2(PostLikeVO postLikeVO) {
        postLikeMapper.deleteByUserAndPost(postLikeVO);
    }

    // 마이페이지 - 사용자가 좋아요를 누른 게시물 문제 둥지 목록 조회
    public List<PostLikeDTO> findLikedQuestionPostsByUserId(Long userId) {
        return postLikeMapper.selectLikedQuestionPostsByUserId(userId);
    }

    // 마이페이지 - 사용자가 좋아요를 누른 OPEN 목록 조회
    public List<PostLikeDTO> findLikedOpenPostsByUserId(Long userId) {
        return postLikeMapper.selectLikedOpenPostsByUserId(userId);
    }


}

