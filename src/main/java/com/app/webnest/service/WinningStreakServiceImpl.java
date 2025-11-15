package com.app.webnest.service;


import com.app.webnest.repository.WinningStreakDAo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class WinningStreakServiceImpl implements WinningStreakService {
    private final WinningStreakDAo winningStreakDAO;
    @Override
    public Integer getWinCountByUserId(Long userId) {
        return winningStreakDAO.findUserWinCount(userId);
    }
}
