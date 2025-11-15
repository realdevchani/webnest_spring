package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.*;
import com.app.webnest.repository.GameRoomDAO;
import com.app.webnest.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game-rooms/*")
public class GameRoomApi {

    private final GameRoomService gameRoomService;
    private final GameJoinService gameJoinService;
    private final WinningStreakService winningStreakService;
    private final FollowService followService;
    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getRooms(@RequestParam Long userId) {
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
        GameRoomDTO room = gameRoomService.getRoom(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("채팅방 목록조회", room));
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

}
