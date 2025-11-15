package com.app.webnest.service;

import com.app.webnest.domain.vo.SubcommentLikeVO;
import com.app.webnest.repository.CommentLikeDAO;
import com.app.webnest.repository.SubcommentLikeDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SubcommentLikeServiceImpl implements SubcommentLikeService {
    private final SubcommentLikeDAO subcommentLikeDAO;

    @Override
    public Map<String, Long> save(SubcommentLikeVO  subcommentLikeVO) {
        Map<String, Long> response = new HashMap<>();
        Long newPostId = subcommentLikeDAO.save(subcommentLikeVO);
        response.put("newPostId", newPostId);
        return response;
    }

    @Override
    public int getSubcommentLike(Long subcommentId) {
        return subcommentLikeDAO.findSubcommentLike(subcommentId);
    }

    @Override
    public void deleteSubcommentLike(Long id) {
        subcommentLikeDAO.remove(id);
    }
    @Override
    public void deleteByUserAndSubcomment(SubcommentLikeVO subcommentLikeVO) {
        subcommentLikeDAO.remove2(subcommentLikeVO);
    }

    @Override
    public Map<String, Object> toggleLike(Long subcommentId, Long userId) {
        boolean isLiked = subcommentLikeDAO.isLiked(subcommentId, userId);

        if (isLiked) {
            subcommentLikeDAO.removeLike(subcommentId, userId);
        } else {
            subcommentLikeDAO.addLike(subcommentId, userId);
        }

        int likeCount = subcommentLikeDAO.countLike(subcommentId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", !isLiked);
        result.put("likeCount", likeCount);

        return result;
    }

}

