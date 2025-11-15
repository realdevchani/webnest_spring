package com.app.webnest.service;

import com.app.webnest.domain.dto.CommentLikeDTO;
import com.app.webnest.domain.vo.CommentLikeVO;

import java.util.Map;

public interface CommentLikeService {

    public int getCommentLike(Long commentId);

    public Map<String, Long> save(CommentLikeDTO commentLikeDTO);

    public void deleteCommentLike(Long id);

    public void deleteByUserAndComment(CommentLikeVO commentLikeVO);

    public Map<String, Object> toggleLike(Long commentId, Long postId, Long userId);
}
