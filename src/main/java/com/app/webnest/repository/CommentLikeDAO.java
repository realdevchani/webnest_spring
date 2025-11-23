package com.app.webnest.repository;

import com.app.webnest.domain.dto.CommentLikeDTO;
import com.app.webnest.domain.vo.CommentLikeVO;
import com.app.webnest.mapper.CommentLikeMapper;
import com.app.webnest.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CommentLikeDAO {
    private final CommentLikeMapper commentLikeMapper;

    public int findCommentLike(Long commentId) {
        return commentLikeMapper.selectByPostIdcount(commentId);
    }

    // 좋아요 추가 (DTO 파라미터)
    public Long save(CommentLikeDTO commentLikeDTO) {
        commentLikeMapper.insert(commentLikeDTO);
        return commentLikeDTO.getId();
    }

    // 좋아요 삭제 (id로)
    public void remove(Long id) {
        commentLikeMapper.delete(id);
    }

    // 좋아요 삭제 (VO로)
    public void remove2(CommentLikeVO commentLikeVO) {
        commentLikeMapper.deleteByUserAndComment(commentLikeVO);
    }

    // 댓글 ID로 댓글 좋아요들 삭제
    public void removeByCommentId(Long commentId) {
        commentLikeMapper.deleteByCommentId(commentId);
    }
//    int selectByPostIdcount (Long commentId);














    public boolean isLiked(Long commentId, Long userId) {
        Map<String, Long> map = new HashMap<>();
        map.put("commentId", commentId);
        map.put("userId", userId);
        return commentLikeMapper.isLiked(map) > 0;
    }

    public void addLike(Long commentId, Long postId, Long userId) {
        Map<String, Long> map = new HashMap<>();
        map.put("commentId", commentId);
        map.put("postId", postId);
        map.put("userId", userId);
        commentLikeMapper.insertLike(map);
    }

    public void removeLike(Long commentId, Long userId) {
        Map<String, Long> map = new HashMap<>();
        map.put("commentId", commentId);
        map.put("userId", userId);
        commentLikeMapper.deleteLike(map);
    }

    public int countLike(Long commentId) {
        return commentLikeMapper.countLike(commentId);
    }
}
