package com.app.webnest.repository;

import com.app.webnest.domain.dto.FollowDTO;
import com.app.webnest.domain.vo.FollowVO;
import com.app.webnest.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FollowDAO {
    private final FollowMapper followMapper;

    // 특정 유저가 팔로잉하는 유저들 조회
    public List<FollowDTO> findFollowingByUserId(Long userId) {
        return followMapper.selectFollowingByUserId(userId);
    }

    // 특정 유저를 팔로우하는 유저들 조회 (팔로워 리스트)
    public List<FollowDTO> findFollowersByUserId(Long userId) {
        return followMapper.selectFollowersByUserId(userId);
    }

    // 팔로우 추가 (DTO 파라미터)
    public Long save(FollowDTO followDTO) {
        followMapper.insert(followDTO);
        return followDTO.getId();
    }
    public List<FollowDTO> selectFollowersByUserId(Long userId) {
        return followMapper.selectFollowingWithPresence(userId);
    }

    // 팔로우 삭제 (id로)
    public void remove(Long id) {
        followMapper.delete(id);
    }

    // 팔로우 삭제 (VO로)
    public void remove2(FollowVO followVO) {
        followMapper.deleteByUserAndFollower(followVO);
    }
}

