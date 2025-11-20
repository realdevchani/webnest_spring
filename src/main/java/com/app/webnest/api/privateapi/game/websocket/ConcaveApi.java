package com.app.webnest.api.privateapi.game.websocket;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.ConcaveVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.GameRoomService;
import com.app.webnest.service.UserServiceImpl;
import com.app.webnest.util.BoardUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConcaveApi {
    private final GameJoinService gameJoinService;
    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserServiceImpl userService;
    private final BoardUtil boardUtil;

    @MessageMapping("/game/concave/ready")
    public void updateReady(GameJoinVO gameJoinVO) {
        log.info("Ready status update requested. gameRoomId: {}, userId: {}, isReady: {}",
                gameJoinVO.getGameRoomId(), gameJoinVO.getUserId(), gameJoinVO.getGameJoinIsReady());

        // 준비 상태 업데이트
        gameJoinService.updateReady(gameJoinVO);

        // 게임 상태 조회
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "READY_UPDATED");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/concave/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }
    @MessageMapping("/game/concave/start")
    public void startGame(GameJoinVO gameJoinVO) {

        // 게임 시작 시 모든 플레이어를 자동으로 준비완료 상태로 변경
        List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        // 모든 플레이어를 준비완료 상태로 변경
        for (GameJoinDTO player : players) {
            GameJoinVO readyVO = new GameJoinVO();
            readyVO.setUserId(player.getUserId());
            readyVO.setGameRoomId(gameJoinVO.getGameRoomId());
            readyVO.setGameJoinIsReady(1);
            gameJoinService.updateReady(readyVO);
        }

        // 게임 시작 전 초기화 (안전장치)
        // 포지션이 0이 아닌 경우를 대비해 모든 포지션을 0으로 초기화
        gameJoinService.resetAllPosition(gameJoinVO.getGameRoomId());

        // 모든 턴을 0으로 설정
        gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId());

        // 게임 상태를 다시 조회하여 최신 상태 가져오기
        List<GameJoinDTO> currentPlayers = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        // 첫 번째 플레이어의 턴을 1로 설정
        if (!currentPlayers.isEmpty()) {
            GameJoinVO firstPlayerVO = new GameJoinVO();
            firstPlayerVO.setUserId(currentPlayers.get(0).getUserId());
            firstPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
            gameJoinService.updateUserTurn(firstPlayerVO);
            log.info("First player turn set. userId: {}, gameRoomId: {}",
                    currentPlayers.get(0).getUserId(), gameJoinVO.getGameRoomId());
        } else {
            log.warn("No players found to set turn. gameRoomId: {}", gameJoinVO.getGameRoomId());
        }

        // 게임 상태 조회 (턴 설정 후)
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
        log.info("Game state after turn set. Players count: {}", gameState.size());
        gameState.forEach(p -> {
            log.info("Player in gameState - userId: {}, myTurn: {}", p.getUserId(), p.isGameJoinMyturn());
        });

        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STARTED");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/concave/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }
    @MessageMapping("/game/concave/state")
    public void getGameState(GameJoinVO gameJoinVO) {
        log.info("Game state requested. gameRoomId: {}", gameJoinVO.getGameRoomId());

        // 게임 상태 조회
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STATE");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/concave/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }
    @MessageMapping("/game/concave/place")
    public void placeStone(ConcaveVO concaveVO) {
        log.info("Place stone requested. gameRoomId: {}, userId: {}, row: {}, col: {}",
                concaveVO.getGameRoomId(), concaveVO.getUserId(), concaveVO.getConcaveRow(), concaveVO.getConcaveCol());

        GameJoinVO gameJoinVO = new GameJoinVO();
        int row = concaveVO.getConcaveRow();
        int col = concaveVO.getConcaveCol();
        Long gameRoomId = concaveVO.getGameRoomId();
        Long userId = concaveVO.getUserId();

        gameJoinVO.setGameRoomId(gameRoomId);
        gameJoinVO.setUserId(userId);
        log.info("gameJoinVO: {}", gameRoomId);
        List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameRoomId);
        GameJoinDTO currentPlayer = players
                                        .stream()
                                        .filter(player -> player.getUserId() == userId)
                                        .findFirst().orElse(null);
//        if(currentPlayer == null || !currentPlayer.isGameJoinMyturn()) {
//            log.warn("유저의 턴이 아닙니다: {}", userId);
//            return;
//        }

        int[][] board = boardUtil.getBoard(gameRoomId);
        if(board[row][col] != 0){
            log.warn("보드가 아직 준비가 안됐어요..!. row: {}, col: {}", row, col);
        return;
        };
        int stone = currentPlayer.isGameJoinMyturn() == true ? 1 : 2;

//        int stone = players.indexOf(currentPlayer) == 0 ? 1 : 2;
        board[row][col] = stone;
        boardUtil.saveBoard(gameRoomId, board);
        List<Map<String, Integer>> winLine = checkWin(board, row, col, stone);


        if(winLine.isEmpty()) {
            boardUtil.switchTurn(gameRoomId, userId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", winLine.isEmpty() ? "MOVE_PLACED" : "GAME_OVER");
        response.put("boardUpdate", board);
        response.put("lastMove", Map.of("row", row, "col", col));
        response.put("gameState", gameJoinService.getArrangeUserByTurn(gameRoomId));
        if (!winLine.isEmpty()) {
            response.put("winLine", winLine);

        }


        simpMessagingTemplate.convertAndSend("/sub/game/concave/room/" + gameRoomId, response);
    }

    @MessageMapping("/game/concave/timeout")
    public void handleTimeout(ConcaveVO concaveVO) {
        GameJoinVO gameJoinVO = new GameJoinVO();
        Long gameRoomId = concaveVO.getGameRoomId();
        Long userId = concaveVO.getUserId();

        gameJoinVO.setGameRoomId(gameRoomId);
        gameJoinVO.setUserId(userId);

        List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameRoomId);
        GameJoinDTO currentPlayer = players.stream().filter(p -> p.getUserId() == userId).findFirst().orElse(null);

        if(currentPlayer == null || !currentPlayer.isGameJoinMyturn()) {
            return;
        }

        int[][] board = boardUtil.getBoard(gameRoomId);

        List<int[]> empties = new ArrayList<>();
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j] == 0) empties.add(new int[]{i, j});
            }
        }
        if(empties.isEmpty()) {
            log.warn("랜덤 착수");
            return;
        }
        int[] pick = empties.get(new Random().nextInt(empties.size()));
        int row = pick[0], col = pick[1];

        int stone = currentPlayer.isGameJoinMyturn() == true ? 1 : 2;
        board[row][col] = stone;
        boardUtil.saveBoard(gameRoomId, board);

        List<Map<String, Integer>> winLine = checkWin(board, row, col, stone);

        if(winLine.isEmpty()) {
            gameJoinService.updateUserTurn(gameJoinVO);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", winLine.isEmpty() ? "MOVE_PLACED" : "GAME_OVER");
        response.put("boardUpdate", board);
        response.put("lastMove", Map.of("row", row, "col", col));
        response.put("gameState", gameJoinService.getArrangeUserByTurn(gameRoomId));
        if (!winLine.isEmpty()) {
            response.put("winLine", winLine);
        }

        simpMessagingTemplate.convertAndSend("/sub/game/concave/room/" + gameRoomId, response);

    }

    private List<Map<String, Integer>> checkWin(int[][] board, int row, int col, int stone) {
        int[][] directions = {{1,0},{0,1},{1,1},{1,-1}};
        int size = board.length;

        for (int[] dir : directions) {
            int count = 1;
            List<Map<String, Integer>> line = new ArrayList<>();
            line.add(Map.of("row", row, "col", col));

            // forward
            int r = row + dir[0], c = col + dir[1];
            while (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == stone) {
                line.add(Map.of("row", r, "col", c));
                count++;
                r += dir[0]; c += dir[1];
            }

            // backward
            r = row - dir[0]; c = col - dir[1];
            while (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == stone) {
                line.add(Map.of("row", r, "col", c));
                count++;
                r -= dir[0]; c -= dir[1];
            }

            if (count >= 5) return line;
        }
        return Collections.emptyList();
    }


}
