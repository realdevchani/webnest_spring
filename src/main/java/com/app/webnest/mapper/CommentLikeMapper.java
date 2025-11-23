package com.app.webnest.mapper;

import com.app.webnest.domain.dto.CommentLikeDTO;
import com.app.webnest.domain.vo.CommentLikeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface CommentLikeMapper {
    // 게시글 상세에서 좋아요 개수
    public int selectByPostIdcount (Long commentId);

    // 좋아요 추가 (DTO 파라미터)
    public void insert(CommentLikeDTO commentLikeDTO);

    // 좋아요 삭제 (id로)
    public void delete(Long id);

    // 좋아요 삭제 (VO로)
    public void deleteByUserAndComment(CommentLikeVO commentLikeVO);

    // 댓글 ID로 댓글 좋아요들 삭제
    public void deleteByCommentId(Long commentId);

    public int isLiked(Map<String, Long> map);

    public void insertLike(Map<String, Long> map);

    public void deleteLike(Map<String, Long> map);

    public int countLike(Long commentId);
}
