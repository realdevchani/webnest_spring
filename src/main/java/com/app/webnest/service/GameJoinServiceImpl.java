package com.app.webnest.service;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.repository.GameJoinDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class GameJoinServiceImpl implements GameJoinService {

    private final GameJoinDAO gameJoinDAO;

    @Override
    public void join(GameJoinVO gameJoinVO) {
        gameJoinDAO.save(gameJoinVO);
    }

    @Override
    public void leave(GameJoinVO gameJoinVO) {
        gameJoinDAO.delete(gameJoinVO);
    }

    @Override
    public List<GameJoinDTO> getPlayers(Long gameRoomId) {
        return gameJoinDAO.getPlayers(gameRoomId);
    }
}
