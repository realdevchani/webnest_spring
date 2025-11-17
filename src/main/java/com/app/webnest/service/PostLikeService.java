package com.app.webnest.service;

import com.app.webnest.domain.dto.PostLikeDTO;
import com.app.webnest.domain.vo.PostLikeVO;

import java.util.List;
import java.util.Map;

public interface PostLikeService {
    public Map<String, Long> save(PostLikeVO postLikeVO);
    public int getPostLike(Long postId);
    public void deletePostLike(Long id);
    public void deleteByUserAndPost(PostLikeVO postLikeVO);
    

}

