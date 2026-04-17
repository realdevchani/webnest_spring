package com.app.webnest.api.privateapi.game.websocket;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.exception.GameJoinException;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.SnakeGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SnakeGameApi {

    private final SnakeGameService snakeGameService;
    private final GameJoinService gameJoinService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private void broadcast(Long gameRoomId, Map<String, Object> response) {
        simpMessagingTemplate.convertAndSend("/sub/game/snake-puzzle/room/" + gameRoomId, response);
    }

    @MessageMapping("/game/snake-puzzle/ready")
    public void updateReady(GameJoinVO gameJoinVO) {
        gameJoinService.updateReady(gameJoinVO);
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "READY_UPDATED");
        response.put("gameState", gameState);
        broadcast(gameJoinVO.getGameRoomId(), response);
    }

    @MessageMapping("/game/snake-puzzle/start")
    public void startGame(GameJoinVO gameJoinVO) {
        broadcast(gameJoinVO.getGameRoomId(), snakeGameService.startGame(gameJoinVO));
    }

    @MessageMapping("/game/snake-puzzle/roll-dice")
    public void rollDice(GameJoinVO gameJoinVO) {
        try {
            broadcast(gameJoinVO.getGameRoomId(), snakeGameService.rollDice(gameJoinVO));
        } catch (Exception e) {
            broadcast(gameJoinVO.getGameRoomId(), Map.of(
                    "type", "DICE_ROLL_ERROR",
                    "message", "주사위 굴리기 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @MessageMapping("/game/snake-puzzle/end-game")
    public void endGame(GameJoinVO gameJoinVO) {
        try {
            broadcast(gameJoinVO.getGameRoomId(), snakeGameService.endGame(gameJoinVO));
        } catch (GameJoinException e) {
            broadcast(gameJoinVO.getGameRoomId(), Map.of(
                    "type", "GAME_END_ERROR",
                    "message", "게임 종료 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @MessageMapping("/game/snake-puzzle/state")
    public void getGameState(GameJoinVO gameJoinVO) {
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
        broadcast(gameJoinVO.getGameRoomId(), Map.of("type", "GAME_STATE", "gameState", gameState));
    }
}