package com.app.webnest.mapper;

import com.app.webnest.domain.dto.FollowDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WinningStreakMapper {
    public Integer selectCountUserWinCountByUserId(Long userId);
}

