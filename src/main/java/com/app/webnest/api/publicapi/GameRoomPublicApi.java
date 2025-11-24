package com.app.webnest.api.publicapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.GameRoomDTO;
import com.app.webnest.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프론트엔드 호환성을 위한 게임방 조회 공개 API
 * GET /game-room/{id}
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class GameRoomPublicApi {

    private final GameRoomService gameRoomService;

    @GetMapping("/game-room/{id}")
    public ResponseEntity<ApiResponseDTO<GameRoomDTO>> getRoom(@PathVariable Long id) {
        log.info("게임방 단일 조회 요청 (공개 경로) - id: {}", id);
        try {
            GameRoomDTO room = gameRoomService.getRoom(id);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게임방 조회 성공", room));
        } catch (Exception e) {
            log.error("게임방 조회 실패 - id: {}, error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.of("게임방을 찾을 수 없습니다. ID: " + id, null));
        }
    }
}

