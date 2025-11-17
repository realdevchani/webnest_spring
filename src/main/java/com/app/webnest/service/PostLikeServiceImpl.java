package com.app.webnest.service;

import com.app.webnest.domain.dto.PostLikeDTO;
import com.app.webnest.domain.vo.PostLikeVO;
import com.app.webnest.repository.PostLikeDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeServiceImpl implements PostLikeService {
    private final PostLikeDAO postLikeDAO;

    @Override
    public Map<String, Long> save(PostLikeVO  postLikeVO) {
        Map<String, Long> response = new HashMap<>();
        Long newPostId = postLikeDAO.save(postLikeVO);
        response.put("newPostId", newPostId);
        return response;
    }

    @Override
    public int getPostLike(Long postId) {
        return postLikeDAO.findPostLike(postId);
    }

    @Override
    public void deletePostLike(Long id) {
        postLikeDAO.remove(id);
    }
    @Override
    public void deleteByUserAndPost(PostLikeVO postLikeVO) {
        postLikeDAO.remove2(postLikeVO);
    }




}

