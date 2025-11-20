package com.app.webnest.util;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.service.GameJoinService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class BoardUtil {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final GameJoinService gameJoinService;

    private static final String BOARD_KEY_PREFIX = "game:board:";

    public int[][] initBoard(Long gameRoomId) {
        int[][] board = new int[15][15];
        saveBoard(gameRoomId, board);
        return board;
    }


    public int[][] getBoard(Long gameRoomId) {
        String key = BOARD_KEY_PREFIX + gameRoomId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return initBoard(gameRoomId);
        }
        try {
            return objectMapper.readValue(json, int[][].class);
        } catch(Exception e) {
            throw new RuntimeException("보드를 가져오지 못했습니다 : Redis", e);
        }
    }

    public void saveBoard(Long gameRoomId, int[][] board) {
        String key = BOARD_KEY_PREFIX + gameRoomId;
        try {
            String json = objectMapper.writeValueAsString(board);
            redisTemplate.opsForValue().set(key, json, 1, TimeUnit.HOURS);
        } catch(Exception e) {
            throw new RuntimeException("보드를 저장하지 못했습니다 : Redis", e);
        }
    }

    public void deleteBoard(Long gameRoomId) {
        String key = BOARD_KEY_PREFIX + gameRoomId;
        redisTemplate.delete(key);
    }


    public void switchTurn(Long gameRoomId, Long userId) {
        List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameRoomId);
        GameJoinVO gameJoinVO = new GameJoinVO();
        gameJoinVO.setGameRoomId(gameRoomId);
        gameJoinVO.setUserId(userId);
        int currentIndex = IntStream.range(0, players.size())
                .filter(i -> players.get(i).getUserId().equals(userId))
                .findFirst()
                .orElse(-1);

        if (currentIndex != -1) {
            int nextIndex = (currentIndex + 1) % players.size();
            Long nextUserId = players.get(nextIndex).getUserId();

            // 모든 턴 초기화
            gameJoinService.updateCurrentUserTurn(gameJoinVO);

            // 다음 유저 턴 설정
            GameJoinVO nextTurnVO = new GameJoinVO();
            nextTurnVO.setUserId(nextUserId);
            nextTurnVO.setGameRoomId(gameRoomId);
            gameJoinService.updateUserTurn(nextTurnVO);

        }
    }



}
