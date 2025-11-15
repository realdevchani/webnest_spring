package com.app.webnest.service;

import com.app.webnest.domain.dto.FollowDTO;
import com.app.webnest.domain.vo.FollowVO;
import com.app.webnest.repository.FollowDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class FollowServiceImpl implements FollowService {
    private final FollowDAO followDAO;

    @Override
    public List<FollowDTO> getFollowingByUserId(Long userId) {
        try {
            List<FollowDTO> following = followDAO.findFollowingByUserId(userId);
            if (following == null || following.isEmpty()) {
                return new ArrayList<>();
            }
            return following;
        } catch (Exception e) {
            log.error("팔로잉 리스트 조회 실패. userId: {}, error: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<FollowDTO> getFollowersByUserId(Long userId) {
        try {
            List<FollowDTO> followers = followDAO.findFollowersByUserId(userId);
            if (followers == null || followers.isEmpty()) {
                return new ArrayList<>();
            }
            return followers;
        } catch (Exception e) {
            log.error("팔로워 리스트 조회 실패. userId: {}, error: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Long> save(FollowDTO followDTO) {
        Map<String, Long> response = new HashMap<>();
        Long newFollowId = followDAO.save(followDTO);
        response.put("newFollowId", newFollowId);
        return response;
    }

    @Override
    public void deleteFollow(Long id) {
        followDAO.remove(id);
    }

    @Override
    public void deleteByUserAndFollower(FollowVO followVO) {
        followDAO.remove2(followVO);
    }

    @Override
    public List<FollowDTO>getFollowWithStatus(Long userId) {
        return followDAO.selectFollowersByUserId(userId);
    }
}

