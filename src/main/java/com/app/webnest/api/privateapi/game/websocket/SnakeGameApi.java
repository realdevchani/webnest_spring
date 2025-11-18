package com.app.webnest.api.privateapi.game.websocket;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.dto.GameRoomDTO;
import com.app.webnest.domain.dto.UserResponseDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.domain.vo.UserVO;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.GameRoomService;
import com.app.webnest.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SnakeGameApi {

    private final GameJoinService gameJoinService;
    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserServiceImpl userService;

    /**
     * 준비 상태 업데이트
     * /pub/game/snake/ready
     * 요청: { gameRoomId: Long, userId: Long, gameJoinIsReady: 1 }
     */
    @MessageMapping("/game/snake-puzzle/ready")
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
                "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }

    /**
     * 게임 시작
     * /pub/game/snake/start
     * 요청: { gameRoomId: Long, userId: Long } (방장의 userId)
     */
    @MessageMapping("/game/snake-puzzle/start")
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
                "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }

    /**
     * 주사위 굴리기 (모든 게임 로직 처리)
     * /pub/game/snake/roll-dice
     * 요청: { gameRoomId: Long, userId: Long, dice1: Integer, dice2: Integer }
     * 
     * 이 메서드에서 처리할 로직:
     * 1. 턴 검증 (getUserTurn)
     * 2. 현재 포지션 조회 (getUserPosition)
     * 3. 주사위 값 검증 (프론트엔드에서 받은 값)
     * 4. 새 포지션 계산 (현재 포지션 + 주사위 합)
     * 5. 함정/지름길 처리 (케이스문으로 처리)
     * 6. 포지션 업데이트 (updateUserPosition)
     * 7. 게임 종료 체크 (포지션 >= 100)
     * 8. 더블 체크 (같은 숫자면 턴 유지, 다르면 턴 넘기기)
     * 9. 턴 넘기기 (getArrangeUserByTurn으로 다음 유저 찾아서 updateUserTurn)
     */
    @MessageMapping("/game/snake-puzzle/roll-dice")
    public void rollDice(GameJoinVO gameJoinVO) {

        try {
        // ========== 비즈니스 로직 작성 영역 ==========
        
        // 1. 턴 검증
         boolean isMyTurn = gameJoinService.getUserTurn(gameJoinVO);
         if (!isMyTurn) {
             log.warn("Not user's turn. userId: {}", gameJoinVO.getUserId());
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("type", "NOT_YOUR_TURN");
             errorResponse.put("message", "현재 당신의 턴이 아닙니다.");
             simpMessagingTemplate.convertAndSend(
                     "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                     errorResponse
             );
             return;
         }
         
         // 게임 종료 체크 (게임이 이미 끝났으면 주사위 굴리기 불가)
         List<GameJoinDTO> currentGameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
         boolean gameAlreadyEnded = currentGameState.stream()
                 .anyMatch(player -> player.getGameJoinPosition() != null && player.getGameJoinPosition() >= 100);
         if (gameAlreadyEnded) {
             log.warn("Game already ended. userId: {}", gameJoinVO.getUserId());
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("type", "GAME_ALREADY_ENDED");
             errorResponse.put("message", "게임이 이미 종료되었습니다.");
             simpMessagingTemplate.convertAndSend(
                     "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                     errorResponse
             );
             return;
         }
        
        // 2. 게임방 정보 조회 (팀전 여부 확인)
        GameRoomDTO gameRoom = gameRoomService.getRoom(gameJoinVO.getGameRoomId());
        boolean isTeamMode = gameRoom.isGameRoomIsTeam();
        
        // 3. 현재 포지션 조회 (팀전이면 팀의 포지션, 개인전이면 개인 포지션)
        Integer currentPosition;
        String userTeamColor = null;
        if (isTeamMode) {
            // 팀전: 현재 유저의 팀 컬러 확인
            List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
            GameJoinDTO currentPlayer = players.stream()
                    .filter(p -> p.getUserId().equals(gameJoinVO.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            userTeamColor = currentPlayer.getGameJoinTeamcolor();
            // 같은 팀의 첫 번째 플레이어 포지션 사용 (팀 공유 포지션)
            final String teamColor = userTeamColor; // effectively final 변수 생성
            currentPosition = players.stream()
                    .filter(p -> teamColor != null && teamColor.equals(p.getGameJoinTeamcolor()))
                    .map(GameJoinDTO::getGameJoinPosition)
                    .findFirst()
                    .orElse(0);
        } else {
            // 개인전: 개인 포지션
            currentPosition = gameJoinService.getUserPosition(gameJoinVO);
            if (currentPosition == null) {
                currentPosition = 0;
            }
        }
        
        // 4. 주사위 값 검증 (프론트엔드에서 받은 값 사용)
         if (gameJoinVO.getDice1() == null || gameJoinVO.getDice2() == null) {
             log.warn("주사위 값이 없습니다. userId: {}, dice1: {}, dice2: {}", 
                     gameJoinVO.getUserId(), gameJoinVO.getDice1(), gameJoinVO.getDice2());
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("type", "INVALID_DICE");
             errorResponse.put("message", "주사위 값이 필요합니다.");
             simpMessagingTemplate.convertAndSend(
                     "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                     errorResponse
             );
             return;
         }
         
         // 주사위 값 유효성 검증 (1~6 사이)
         int dice1 = gameJoinVO.getDice1();
         int dice2 = gameJoinVO.getDice2();
         if (dice1 < 1 || dice1 > 6 || dice2 < 1 || dice2 > 6) {
             log.warn("유효하지 않은 주사위 값. userId: {}, dice1: {}, dice2: {}", 
                     gameJoinVO.getUserId(), dice1, dice2);
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("type", "INVALID_DICE");
             errorResponse.put("message", "주사위 값은 1~6 사이여야 합니다.");
             simpMessagingTemplate.convertAndSend(
                     "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                     errorResponse
             );
             return;
         }
         
         log.info("주사위 굴리기 - userId: {}, dice1: {}, dice2: {}", gameJoinVO.getUserId(), dice1, dice2);
        
        // 5. 새 포지션 계산
         int newPosition = currentPosition + dice1 + dice2;
        
        // 6. 함정/지름길 처리 (100 이상이 아닐 때만 적용)
         String boardType = null; // "TRAP" 또는 "LADDER"
         if (newPosition < 100) {
             switch (newPosition) {
             // 뱀 (함정) - 아래로 내려감
             case 99:
                 newPosition = 65;
                 boardType = "TRAP";
                 break;
             case 95:
                 newPosition = 75;
                 boardType = "TRAP";
                 break;
             case 87:
                 newPosition = 24;
                 boardType = "TRAP";
                 break;
             case 64:
                 newPosition = 43;
                 boardType = "TRAP";
                 break;
             case 59:
                 newPosition = 2;
                 boardType = "TRAP";
                 break;
             case 36:
                 newPosition = 6;
                 boardType = "TRAP";
                 break;
             case 28:
                 newPosition = 10;
                 boardType = "TRAP";
                 break;
             case 16:
                 newPosition = 3;
                 boardType = "TRAP";
                 break;
             // 사다리 (지름길) - 위로 올라감
             case 4:
                 newPosition = 25;
                 boardType = "LADDER";
                 break;
             case 27:
                 newPosition = 48;
                 boardType = "LADDER";
                 break;
             case 33:
                 newPosition = 63;
                 boardType = "LADDER";
                 break;
             case 42:
                 newPosition = 60;
                 boardType = "LADDER";
                 break;
             case 50:
                 newPosition = 69;
                 boardType = "LADDER";
                 break;
             case 62:
                 newPosition = 81;
                 boardType = "LADDER";
                 break;
             case 74:
                 newPosition = 92;
                 boardType = "LADDER";
                 break;
             }
         }
        
        // 7. 포지션 업데이트 (팀전이면 같은 팀의 모든 플레이어 포지션 업데이트)
         if (isTeamMode && userTeamColor != null) {
             // 팀전: 같은 팀의 모든 플레이어 포지션 업데이트
             List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
             final String teamColor = userTeamColor; // effectively final 변수 생성
             List<GameJoinDTO> teamPlayers = players.stream()
                     .filter(player -> teamColor.equals(player.getGameJoinTeamcolor()))
                     .collect(Collectors.toList());
             
             for (GameJoinDTO teamPlayer : teamPlayers) {
                 GameJoinVO teamPlayerVO = new GameJoinVO();
                 teamPlayerVO.setUserId(teamPlayer.getUserId());
                 teamPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
                 teamPlayerVO.setGameJoinPosition(newPosition);
                 gameJoinService.updateUserPosition(teamPlayerVO);
             }
         } else {
             // 개인전: 개인 포지션만 업데이트
             gameJoinVO.setGameJoinPosition(newPosition);
             gameJoinService.updateUserPosition(gameJoinVO);
         }
        
        // 8. 게임 종료 체크 (100 이상이면 승리)
         boolean gameEnded = newPosition >= 100;
            List<GameJoinDTO> currentPlayer = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
         if (gameEnded) {
            Long winnerId = gameJoinVO.getUserId();
            UserResponseDTO winnerUser = userService.getUserById(winnerId);
            UserVO winnerUserVO = new UserVO(winnerUser);
            log.info("Game ended! Winner: userId {}", gameJoinVO.getUserId());
            gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId()); // 모든 턴 종료
            gameJoinService.resetAllPosition(gameJoinVO.getGameRoomId()); // 모든 포지션 0으로 초기화
            gameJoinService.resetAllReady(gameJoinVO.getGameRoomId()); // 모든 레디 상태 0으로 초기화
            winnerUserVO.setUserExp(winnerUserVO.getUserExp() + 100);
            userService.modify(winnerUserVO);
                // 경험치 추가 등 (추후 구현)
             List<GameJoinDTO> updateUserExp = currentPlayer.stream().map((player) ->{
                 player.setUserExp(player.getUserExp() + 100);
                 return player;
             }).toList();
             updateUserExp.forEach((player) -> {
                 userService.modifyUserEXPByGameResult(player);
             });
         }
        
        // 9. 더블 체크 및 턴 넘기기
         boolean isDouble = dice1 == dice2;
         if (!isDouble && !gameEnded) {
             // 턴 넘기기 로직
             gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId()); // 모든 유저 턴 0으로
             List<GameJoinDTO> players = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
             // 현재 유저의 인덱스 찾기
             int currentIndex = -1;
             for (int i = 0; i < players.size(); i++) {
                 if (players.get(i).getUserId().equals(gameJoinVO.getUserId())) {
                     currentIndex = i;
                     break;
                 }
             }
            // 다음 유저의 턴 활성화
            if (currentIndex >= 0 && currentIndex < players.size() - 1) {
                GameJoinVO nextPlayerVO = new GameJoinVO();
                nextPlayerVO.setUserId(players.get(currentIndex + 1).getUserId());
                nextPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
                gameJoinService.updateUserTurn(nextPlayerVO);
            } else if (currentIndex == players.size() - 1) {
                // 마지막 유저면 첫 번째 유저로
                GameJoinVO firstPlayerVO = new GameJoinVO();
                firstPlayerVO.setUserId(players.get(0).getUserId());
                firstPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
                gameJoinService.updateUserTurn(firstPlayerVO);
            }
         }
        
        // ============================================
        
        // 게임 상태 조회 (게임 종료 후 초기화된 상태 반영)
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
        
        Map<String, Object> response = new HashMap<>();
        if (gameEnded) {
            response.put("type", "GAME_ENDED");
            // 승자 정보 찾기
            GameJoinDTO winner = gameState.stream()
                    .filter(p -> p.getUserId().equals(gameJoinVO.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (winner != null) {
                response.put("winner", winner);
                response.put("winnerUserId", winner.getUserId());
                response.put("winnerNickname", winner.getUserNickname());
            }
        } else {
            response.put("type", "DICE_ROLLED");
        }
        response.put("gameState", gameState);
         response.put("dice1", dice1);
         response.put("dice2", dice2);
         response.put("isDouble", isDouble);
         response.put("gameEnded", gameEnded);
        response.put("boardType", boardType); // "TRAP" 또는 "LADDER" 또는 null
        response.put("newPosition", newPosition);
        
        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                response
        );
        log.info("Dice roll completed. dice1: {}, dice2: {}, newPosition: {}, isDouble: {}", 
                dice1, dice2, newPosition, isDouble);
        } catch (Exception e) {
            log.error("Error in rollDice. gameRoomId: {}, userId: {}", 
                    gameJoinVO.getGameRoomId(), gameJoinVO.getUserId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "DICE_ROLL_ERROR");
            errorResponse.put("message", "주사위 굴리기 중 오류가 발생했습니다: " + e.getMessage());
            simpMessagingTemplate.convertAndSend(
                    "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                    errorResponse
            );
        }
    }

    /**
     * 게임 상태 조회 (선택적)
     * /pub/game/snake/state
     * 요청: { gameRoomId: Long }
     */
    @MessageMapping("/game/snake-puzzle/state")
    public void getGameState(GameJoinVO gameJoinVO) {
        log.info("Game state requested. gameRoomId: {}", gameJoinVO.getGameRoomId());
        
        // 게임 상태 조회
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STATE");
        response.put("gameState", gameState);
        
        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/snake-puzzle/room/" + gameJoinVO.getGameRoomId(),
                response
        );
    }
}

