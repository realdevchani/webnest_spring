package com.app.webnest.service;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.dto.UserResponseDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.domain.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SnakeGameService {

    private final GameRoomService gameRoomService;
    private final GameJoinService gameJoinService;
    private final UserServiceImpl userService;

    public Map<String, Object> startGame(GameJoinVO gameJoinVO) {
        List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        for (GameJoinDTO player : players) {
            GameJoinVO readyVO = new GameJoinVO();
            readyVO.setUserId(player.getUserId());
            readyVO.setGameRoomId(gameJoinVO.getGameRoomId());
            readyVO.setGameJoinIsReady(1);
            gameJoinService.updateReady(readyVO);
        }

        gameJoinService.resetAllPosition(gameJoinVO.getGameRoomId());
        gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId());

        GameJoinVO firstPlayerVO = new GameJoinVO();
        firstPlayerVO.setUserId(gameJoinVO.getUserId());
        firstPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
        gameJoinService.updateUserTurn(firstPlayerVO);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STARTED");
        response.put("gameState", gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId()));
        return response;
    }

    public Map<String, Object> rollDice(GameJoinVO gameJoinVO) {
        // 1. 턴 검증
        if (!gameJoinService.getUserTurn(gameJoinVO)) {
            return Map.of("type", "NOT_YOUR_TURN", "message", "현재 당신의 턴이 아닙니다.");
        }

        // 2. 게임 종료 여부 체크
        boolean gameAlreadyEnded = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId()).stream()
                .anyMatch(p -> p.getGameJoinPosition() != null && p.getGameJoinPosition() >= 100);
        if (gameAlreadyEnded) {
            return Map.of("type", "GAME_ALREADY_ENDED", "message", "게임이 이미 종료되었습니다.");
        }

        // 3. 주사위 검증
        if (gameJoinVO.getDice1() == null || gameJoinVO.getDice2() == null) {
            return Map.of("type", "INVALID_DICE", "message", "주사위 값이 필요합니다.");
        }
        int dice1 = gameJoinVO.getDice1();
        int dice2 = gameJoinVO.getDice2();
        if (dice1 < 1 || dice1 > 6 || dice2 < 1 || dice2 > 6) {
            return Map.of("type", "INVALID_DICE", "message", "주사위 값은 1~6 사이여야 합니다.");
        }

        // 4. 포지션 계산
        int currentPosition = getCurrentPosition(gameJoinVO);
        HashMap<String, Object> calcResult = calculateUserPosition(currentPosition + dice1 + dice2);
        int newPosition = (int) calcResult.get("newPosition");
        String boardType = (String) calcResult.get("boardType");

        // 5. 포지션 업데이트
        updatePosition(gameJoinVO, newPosition);

        // 6. 게임 종료 / 더블 / 턴 처리
        boolean gameEnded = newPosition >= 100;
        boolean isDouble = checkDouble(dice1, dice2);
        if (!isDouble && !gameEnded) {
            changeTurn(gameJoinVO);
        }

        // 7. 응답 구성
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
        Map<String, Object> response = new HashMap<>();

        if (gameEnded) {
            response.put("type", "WINNER_DETECTED");
            gameState.stream()
                    .filter(p -> p.getUserId().equals(gameJoinVO.getUserId()))
                    .findFirst()
                    .ifPresent(winner -> {
                        response.put("winner", winner);
                        response.put("winnerUserId", winner.getUserId());
                        response.put("winnerNickname", winner.getUserNickname());
                    });
        } else {
            response.put("type", "DICE_ROLLED");
        }
        response.put("gameState", gameState);
        response.put("dice1", dice1);
        response.put("dice2", dice2);
        response.put("isDouble", isDouble);
        response.put("gameEnded", gameEnded);
        response.put("boardType", boardType);
        response.put("newPosition", newPosition);
        return response;
    }

    public Map<String, Object> endGame(GameJoinVO gameJoinVO) {
        Long winnerId = gameJoinVO.getUserId();
        UserResponseDTO winnerUser = userService.getUserById(winnerId);
        UserVO winnerUserVO = new UserVO(winnerUser);

        List<GameJoinDTO> currentPlayers = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId());
        gameJoinService.resetAllPosition(gameJoinVO.getGameRoomId());
        gameJoinService.resetAllReady(gameJoinVO.getGameRoomId());

        winnerUserVO.setUserExp(winnerUserVO.getUserExp() + 40);
        userService.modify(winnerUserVO);

        currentPlayers.forEach(player -> {
            player.setUserExp(player.getUserExp() + 30);
            userService.modifyUserEXPByGameResult(player);
        });

        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_ENDED");
        response.put("gameState", gameState);
        gameState.stream()
                .filter(p -> p.getUserId().equals(winnerId))
                .findFirst()
                .ifPresent(winner -> {
                    response.put("winner", winner);
                    response.put("winnerUserId", winner.getUserId());
                    response.put("winnerNickname", winner.getUserNickname());
                });
        return response;
    }

    private Integer getCurrentPosition(GameJoinVO gameJoinVO) {
        boolean isTeamMode = gameRoomService.getRoom(gameJoinVO.getGameRoomId()).isGameRoomIsTeam();
        if (isTeamMode) {
            List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
            String teamColor = players.stream()
                    .filter(p -> p.getUserId().equals(gameJoinVO.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Player not found"))
                    .getGameJoinTeamcolor();
            return players.stream()
                    .filter(p -> teamColor.equals(p.getGameJoinTeamcolor()))
                    .map(GameJoinDTO::getGameJoinPosition)
                    .findFirst()
                    .orElse(0);
        } else {
            Integer position = gameJoinService.getUserPosition(gameJoinVO);
            return position != null ? position : 0;
        }
    }

    private boolean checkDouble(int dice1, int dice2) {
        return dice1 == dice2;
    }

    private void changeTurn(GameJoinVO gameJoinVO) {
        gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId());
        List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        int currentIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserId().equals(gameJoinVO.getUserId())) {
                currentIndex = i;
                break;
            }
        }

        GameJoinVO nextPlayerVO = new GameJoinVO();
        nextPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
        if (currentIndex >= 0 && currentIndex < players.size() - 1) {
            nextPlayerVO.setUserId(players.get(currentIndex + 1).getUserId());
        } else {
            nextPlayerVO.setUserId(players.get(0).getUserId());
        }
        gameJoinService.updateUserTurn(nextPlayerVO);
    }

    private void updatePosition(GameJoinVO gameJoinVO, int newPosition) {
        boolean isTeamMode = gameRoomService.getRoom(gameJoinVO.getGameRoomId()).isGameRoomIsTeam();
        if (isTeamMode) {
            List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
            String userTeamColor = players.stream()
                    .filter(p -> p.getUserId().equals(gameJoinVO.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Player not found"))
                    .getGameJoinTeamcolor();
            players.stream()
                    .filter(p -> userTeamColor.equals(p.getGameJoinTeamcolor()))
                    .forEach(teamPlayer -> {
                        GameJoinVO teamPlayerVO = new GameJoinVO();
                        teamPlayerVO.setUserId(teamPlayer.getUserId());
                        teamPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
                        teamPlayerVO.setGameJoinPosition(newPosition);
                        gameJoinService.updateUserPosition(teamPlayerVO);
                    });
        } else {
            gameJoinVO.setGameJoinPosition(newPosition);
            gameJoinService.updateUserPosition(gameJoinVO);
        }
    }

    private HashMap<String, Object> calculateUserPosition(int newPosition) {
        String boardType = "DEFAULT";
        if (newPosition < 100) {
            switch (newPosition) {
                case 99: newPosition = 65; boardType = "TRAP"; break;
                case 95: newPosition = 75; boardType = "TRAP"; break;
                case 87: newPosition = 24; boardType = "TRAP"; break;
                case 64: newPosition = 43; boardType = "TRAP"; break;
                case 59: newPosition = 2;  boardType = "TRAP"; break;
                case 36: newPosition = 6;  boardType = "TRAP"; break;
                case 28: newPosition = 10; boardType = "TRAP"; break;
                case 16: newPosition = 3;  boardType = "TRAP"; break;
                case 4:  newPosition = 25; boardType = "LADDER"; break;
                case 27: newPosition = 48; boardType = "LADDER"; break;
                case 33: newPosition = 63; boardType = "LADDER"; break;
                case 42: newPosition = 60; boardType = "LADDER"; break;
                case 50: newPosition = 69; boardType = "LADDER"; break;
                case 62: newPosition = 81; boardType = "LADDER"; break;
                case 74: newPosition = 92; boardType = "LADDER"; break;
            }
        }
        HashMap<String, Object> calcResult = new HashMap<>();
        calcResult.put("boardType", boardType);
        calcResult.put("newPosition", newPosition);
        return calcResult;
    }
}