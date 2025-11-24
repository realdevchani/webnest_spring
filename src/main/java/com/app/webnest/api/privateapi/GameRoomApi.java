package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.*;
import com.app.webnest.exception.UserException;
import com.app.webnest.repository.GameRoomDAO;
import com.app.webnest.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.app.webnest.domain.vo.GameRoomVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.service.AuthService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/private/game-rooms")
public class GameRoomApi {

    private final GameRoomService gameRoomService;
    private final GameJoinService gameJoinService;
    private final WinningStreakService winningStreakService;
    private final FollowService followService;
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getRooms(@RequestParam Long userId) {
        log.info("게임방 목록 조회 요청 - userId: {}", userId);
        List<GameRoomDTO> rooms = gameRoomService.getRooms(userId);
        Integer winCount = winningStreakService.getWinCountByUserId(userId);
        List<FollowDTO> following = followService.getFollowWithStatus(userId);
        UserResponseDTO myInfo = userService.getUserById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("roomList", rooms);
        response.put("winningCount", winCount != null ? winCount : 0);
        response.put("following", following);
        response.put("myInfo", myInfo);
        
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("채팅방 목록조회", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GameRoomDTO>> getRoom(@PathVariable Long id) {
        log.info("게임방 단일 조회 요청 - id: {}", id);
        try {
            GameRoomDTO room = gameRoomService.getRoom(id);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게임방 조회 성공", room));
        } catch (Exception e) {
            log.error("게임방 조회 실패 - id: {}, error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.of("게임방을 찾을 수 없습니다. ID: " + id, null));
        }
    }


    /**
     * 게임 상태 조회 (플레이어 위치, 턴, 레디 상태 등)
     * GET /private/game-rooms/{gameRoomId}/game-state
     * 채팅의 getChats처럼 초기 로드 시 사용
     */

    @GetMapping("/{gameRoomId}/game-state")
    public ResponseEntity<ApiResponseDTO<List<GameJoinDTO>>> getGameState(@PathVariable Long gameRoomId) {
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameRoomId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.of("게임 상태 조회 성공", gameState));
    }

    /**
     * 게임방 생성
     * POST /private/game-rooms
     * RequestBody: { gameRoomVO: {...} }
     * userId는 Authentication에서 자동으로 가져옴
     */
    @PostMapping("")
    public ResponseEntity<ApiResponseDTO<GameRoomDTO>> createRoom(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        // Authentication에서 userId 가져오기
        String email = authService.getUserEmailFromAuthentication(authentication);
        if (email == null || email.isBlank()) {
            throw new UserException("인증 정보에 이메일이 없습니다.");
        }
        Long userId = userService.getUserIdByUserEmail(email);
        
        // GameRoomVO 파싱
        @SuppressWarnings("unchecked")
        Map<String, Object> roomData = (Map<String, Object>) request.get("gameRoomVO");
        
        GameRoomVO gameRoomVO = new GameRoomVO();
        if (roomData.get("gameRoomTitle") != null) gameRoomVO.setGameRoomTitle(roomData.get("gameRoomTitle").toString());
        if (roomData.get("gameRoomIsTeam") != null) gameRoomVO.setGameRoomIsTeam(Integer.valueOf(roomData.get("gameRoomIsTeam").toString()));
        if (roomData.get("gameRoomType") != null) gameRoomVO.setGameRoomType(roomData.get("gameRoomType").toString());
        if (roomData.get("gameRoomMaxPlayer") != null) gameRoomVO.setGameRoomMaxPlayer(Integer.valueOf(roomData.get("gameRoomMaxPlayer").toString()));
        if (roomData.get("gameRoomIsStart") != null) gameRoomVO.setGameRoomIsStart(Integer.valueOf(roomData.get("gameRoomIsStart").toString()));
        if (roomData.get("gameRoomIsOpen") != null) gameRoomVO.setGameRoomIsOpen(Integer.valueOf(roomData.get("gameRoomIsOpen").toString()));
        if (roomData.get("gameRoomPassKey") != null) gameRoomVO.setGameRoomPassKey(roomData.get("gameRoomPassKey").toString());
        if (roomData.get("gameRoomLanguage") != null) gameRoomVO.setGameRoomLanguage(roomData.get("gameRoomLanguage").toString());
        if (roomData.get("gameRoomDifficult") != null) gameRoomVO.setGameRoomDifficult(Integer.valueOf(roomData.get("gameRoomDifficult").toString()));
        
        // 생성 시간 자동 설정
        gameRoomVO.setGameRoomCreateAt(LocalDateTime.now());
        
        // 게임방 생성과 호스트 추가를 한 트랜잭션에서 처리 (생성된 게임방 DTO 반환)
        GameRoomDTO createdRoom = gameRoomService.createRoomWithHost(gameRoomVO, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.of("게임방 생성 성공", createdRoom));
    }

    /**
     * 게임방 수정
     * PUT /private/game-rooms/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GameRoomDTO>> updateRoom(
            @PathVariable Long id,
            @RequestBody GameRoomVO gameRoomVO) {
        gameRoomVO.setId(id);
        gameRoomService.update(gameRoomVO);
        GameRoomDTO updatedRoom = gameRoomService.getRoom(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.of("게임방 수정 성공", updatedRoom));
    }

    /**
     * 게임방 삭제
     * DELETE /private/game-rooms/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteRoom(@PathVariable Long id) {
        gameRoomService.delete(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.of("게임방 삭제 성공", null));
    }

}
