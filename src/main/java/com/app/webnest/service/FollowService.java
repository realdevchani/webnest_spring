package com.app.webnest.service;

import com.app.webnest.domain.dto.FollowDTO;
import com.app.webnest.domain.vo.FollowVO;

import java.util.List;
import java.util.Map;

public interface FollowService {
    // 특정 유저가 팔로잉하는 유저들 조회
    public List<FollowDTO> getFollowingByUserId(Long userId);
    
    // 특정 유저를 팔로우하는 유저들 조회 (팔로워 리스트)
    public List<FollowDTO> getFollowersByUserId(Long userId);
    
    // 팔로우 추가
    public Map<String, Long> save(FollowDTO followDTO);
    
    // 팔로우 삭제 (id로)
    public void deleteFollow(Long id);
    
    // 팔로우 삭제 (VO로)
    public void deleteByUserAndFollower(FollowVO followVO);

    public List<FollowDTO> getFollowWithStatus (Long userId);
}

