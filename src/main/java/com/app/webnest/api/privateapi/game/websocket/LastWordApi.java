package com.app.webnest.api.privateapi.game.websocket;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.dto.LastWordDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.exception.LastWordException;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.LastWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LastWordApi {
    private final GameJoinService gameJoinService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final LastWordService lastWordService;

    @MessageMapping("/game/last-word/ready")
    public ResponseEntity<ApiResponseDTO> updateReady(GameJoinVO gameJoinVO) {
        // 준비 상태 업데이트
        gameJoinService.updateReady(gameJoinVO);

        // 게임 상태 조회
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "READY_UPDATED");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameJoinVO.getGameRoomId(),
                response
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("플레이어 준비상태 조회", response));
    }

    @MessageMapping("/game/last-word/start")
    public ResponseEntity<ApiResponseDTO> startGame(GameJoinVO gameJoinVO) {

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

        // 모든 턴을 0으로 설정
        gameJoinService.updateAllUserTurn(gameJoinVO.getGameRoomId());

        // 게임 상태를 다시 조회하여 최신 상태 가져오기
        List<GameJoinDTO> currentPlayers = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        // 첫 번째 플레이어의 턴을 1로 설정 -> 방장이 턴 트루 상태 나머지는 다 false
        if (!currentPlayers.isEmpty()) {
            GameJoinVO firstPlayerVO = new GameJoinVO();
            firstPlayerVO.setUserId(currentPlayers.get(0).getUserId());
            firstPlayerVO.setGameRoomId(gameJoinVO.getGameRoomId());
            gameJoinService.updateUserTurn(firstPlayerVO);
        } else {
            throw new LastWordException("/game/last-word/start, startGame(GameJoinVO gameJoinVO), 게임 방 조회 잘못됨 : 게임룸에 포함된 유저 없음");
        }

        // 게임 상태 조회 (턴 설정 후)
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

//        게임 상태를 started 로 바꿈.
        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STARTED");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameJoinVO.getGameRoomId(),
                response
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("게임 상태 시작", response));
    }
    @MessageMapping("/game/last-word/state")
    public ResponseEntity<ApiResponseDTO> getGameState(GameJoinVO gameJoinVO) {
        // 게임 상태 조회
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameJoinVO.getGameRoomId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_STATE");
        response.put("gameState", gameState);

        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameJoinVO.getGameRoomId(),
                response
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("게임 상태 조회", response));
    }

    @MessageMapping("/game/last-word/submit")
    public ResponseEntity<ApiResponseDTO> submitWord(Map<String, Object> request) {
        try {
            // 요청 데이터 추출
            Long userId = request.get("userId") != null ? 
                    Long.valueOf(request.get("userId").toString()) : null;
            Long gameRoomId = request.get("gameRoomId") != null ? 
                    Long.valueOf(request.get("gameRoomId").toString()) : null;
            String word = request.get("word") != null ? 
                    request.get("word").toString() : null;

            if (gameRoomId == null || word == null) {
                throw new LastWordException("필수 파라미터가 없습니다. gameRoomId = " + gameRoomId + ", word = " + word);
            }

            // LastWordDTO 생성
            LastWordDTO lastWordDTO = new LastWordDTO();
            lastWordDTO.setWord(word);
            lastWordDTO.setGameRoomId(gameRoomId);
            lastWordDTO.setUserId(userId);
            lastWordDTO.setFocus(true);
            lastWordDTO.setExplanation("");

            // 단어 검증 및 브로드캐스트
            lastWordService.broadcastWord(lastWordDTO, gameRoomId);
        } catch (Exception e) {
            throw new LastWordException("단어 제출 처리 중 오류 발생");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("단어 제출 요청 수신"));
    }
}
