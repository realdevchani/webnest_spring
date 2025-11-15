package com.app.webnest.repository;

import com.app.webnest.mapper.WinningStreakMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WinningStreakDAo {
    private final WinningStreakMapper winningStreakMapper;

    public Integer findUserWinCount(Long userId){
        return winningStreakMapper.selectCountUserWinCountByUserId(userId);
    }

}
