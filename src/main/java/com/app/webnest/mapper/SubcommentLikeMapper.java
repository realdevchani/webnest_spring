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

    // Toggle 기능을 위한 메서드들
    public int isLiked(Map<String, Long> map);

    public void insertLike(Map<String, Long> map);

    public void deleteLike(Map<String, Long> map);

    public int countLike(Long subcommentId);
}
