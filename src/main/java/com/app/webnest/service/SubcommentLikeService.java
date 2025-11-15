package com.app.webnest.service;

import com.app.webnest.domain.vo.SubcommentLikeVO;

import java.util.Map;

public interface SubcommentLikeService {
    public Map<String, Long> save(SubcommentLikeVO subcommentLikeVO);
    public int getSubcommentLike(Long subcommentId);
    public void deleteSubcommentLike(Long id);
    public void deleteByUserAndSubcomment(SubcommentLikeVO subcommentLikeVO);
    public Map<String, Object> toggleLike(Long subcommentId, Long userId);
}
