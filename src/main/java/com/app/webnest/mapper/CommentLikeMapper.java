package com.app.webnest.mapper;

import com.app.webnest.domain.dto.CommentLikeDTO;
import com.app.webnest.domain.vo.CommentLikeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface CommentLikeMapper {
    // 게시글 상세에서 좋아요 개수
    int selectByPostIdcount (Long commentId);

    // 좋아요 추가 (DTO 파라미터)
    void insert(CommentLikeDTO commentLikeDTO);

    // 좋아요 삭제 (id로)
    void delete(Long id);

    // 좋아요 삭제 (VO로)
    void deleteByUserAndComment(CommentLikeVO commentLikeVO);

    public int isLiked(Map<String, Long> map);

    public void insertLike(Map<String, Long> map);

    public void deleteLike(Map<String, Long> map);

    public int countLike(Long commentId);
}
