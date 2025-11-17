package com.app.webnest.mapper;

import com.app.webnest.domain.dto.PostLikeDTO;
import com.app.webnest.domain.vo.PostLikeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostLikeMapper {

    void insert(PostLikeVO postLikeVO);

    // 게시글 상세에서 좋아요 개수
    public int selectByPostIdcount (Long postId);

    public void delete (Long id);

    void deleteByUserAndPost(PostLikeVO postLikeVO);

    // 마이페이지 - 사용자가 좋아요를 누른 게시물 문제 둥지 목록 조회
    List<PostLikeDTO> selectLikedQuestionPostsByUserId(Long userId);
    
    // 마이페이지 - 사용자가 좋아요를 누른 OPEN 목록 조회
    List<PostLikeDTO> selectLikedOpenPostsByUserId(Long userId);
    
    // 마이페이지 - 사용자가 좋아요를 누른 모든 게시물 목록 조회
    List<PostLikeDTO> selectLikedPostsByUserId(Long userId);
}

