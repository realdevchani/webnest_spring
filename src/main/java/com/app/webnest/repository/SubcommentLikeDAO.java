package com.app.webnest.repository;

import com.app.webnest.domain.vo.SubcommentLikeVO;
import com.app.webnest.mapper.SubcommentLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
@Repository
@RequiredArgsConstructor
public class SubcommentLikeDAO {
    private final SubcommentLikeMapper subcommentLikeMapper;

    public Long save(SubcommentLikeVO subcommentLikeVO) {
        subcommentLikeMapper.insert(subcommentLikeVO);
        return subcommentLikeVO.getId();
    }


    public int findSubcommentLike(Long subcommentId) {
        return subcommentLikeMapper.selectByPostIdcount(subcommentId);
    }

    public void remove(Long id) {
        subcommentLikeMapper.delete(id);
    }

    public void remove2(SubcommentLikeVO subcommentLikeVO) {
        subcommentLikeMapper.deleteByUserAndSubcomment(subcommentLikeVO);
    }

    // 대댓글 ID로 대댓글 좋아요들 삭제
    public void removeBySubcommentId(Long subcommentId) {
        subcommentLikeMapper.deleteBySubcommentId(subcommentId);
    }

}
