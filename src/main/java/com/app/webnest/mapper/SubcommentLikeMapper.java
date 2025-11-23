package com.app.webnest.mapper;

import com.app.webnest.domain.vo.SubcommentLikeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface SubcommentLikeMapper {

    void insert(SubcommentLikeVO subcommentLikeVO);

    // 게시글 상세에서 좋아요 개수
    public int selectByPostIdcount (Long subcommentId);

    public void delete (Long id);

    void deleteByUserAndSubcomment(SubcommentLikeVO subcommentLikeVO);

    // 대댓글 ID로 대댓글 좋아요들 삭제
    void deleteBySubcommentId(Long subcommentId);

}
