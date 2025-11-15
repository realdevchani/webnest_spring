package com.app.webnest.repository;

import com.app.webnest.domain.vo.SubcommentLikeVO;
import com.app.webnest.mapper.CommentLikeMapper;
import com.app.webnest.mapper.SubcommentLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

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

    // Toggle 기능을 위한 메서드들
    public boolean isLiked(Long subcommentId, Long userId) {
        Map<String, Long> map = new HashMap<>();
        map.put("subcommentId", subcommentId);
        map.put("userId", userId);
        return subcommentLikeMapper.isLiked(map) > 0;
    }

    public void addLike(Long subcommentId, Long userId) {
        Map<String, Long> map = new HashMap<>();
        map.put("subcommentId", subcommentId);
        map.put("userId", userId);
        subcommentLikeMapper.insertLike(map);
    }

    public void removeLike(Long subcommentId, Long userId) {
        Map<String, Long> map = new HashMap<>();
        map.put("subcommentId", subcommentId);
        map.put("userId", userId);
        subcommentLikeMapper.deleteLike(map);
    }

    public int countLike(Long subcommentId) {
        return subcommentLikeMapper.countLike(subcommentId);
    }

}
