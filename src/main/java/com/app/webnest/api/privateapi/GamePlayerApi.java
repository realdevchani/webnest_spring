package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.service.GameJoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/player")
public class GamePlayerApi {

    private final GameJoinService gameJoinService;

//    @PostMapping("/{roomId}")
//    public ResponseEntity<ApiResponseDTO> invitePlayers(@ResponseBody UserVO userVO) {
//        List<GameJoinDTO> players = gameJoinService.getPlayers(roomId);
//        return ResponseEntity.status(HttpStatus.).body(ApiResponseDTO.of("게임방 유저 추가"));
//    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponseDTO<List<GameJoinDTO>>> getPlayers(@PathVariable Long roomId) {
        List<GameJoinDTO> players = gameJoinService.getPlayers(roomId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("게임방 유저 전체 조회", players));
    }

    @PutMapping("/{roomId}/{userId}")
    public ResponseEntity<ApiResponseDTO<GameJoinDTO>> updatePlayer(
  //버그 -> 수정이 되면서 증식
            @PathVariable Long roomId,
            @PathVariable Long userId,
            @RequestBody GameJoinDTO gameJoinDTO
    ) {
        if(!roomId.equals(gameJoinDTO.getGameRoomId()) || !userId.equals(gameJoinDTO.getUserId())){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        GameJoinVO gameJoinVO = new GameJoinVO();
        gameJoinVO.setUserId(userId);
        gameJoinVO.setGameRoomId(roomId);
        gameJoinVO.setGameJoinIsHost(gameJoinDTO.isGameJoinIsHost() ? 1 : 0);
        gameJoinVO.setGameJoinTeamcolor(gameJoinDTO.getGameJoinTeamcolor());
        gameJoinVO.setGameJoinMyturn(gameJoinDTO.isGameJoinMyturn() ? 1 : 0);
        gameJoinVO.setGameJoinProfileText(gameJoinDTO.getGameJoinProfileText());

        log.info("업데이트 값 : {}", gameJoinVO);
        gameJoinService.update(gameJoinVO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDTO.of("인게임 사용자 정보 업데이트"));
    }

    @DeleteMapping("/{roomId}/{userId}")
    public ResponseEntity<ApiResponseDTO> resignPlayer(@PathVariable Long roomId, @PathVariable Long userId) {
        GameJoinVO gameJoinVO = new GameJoinVO();
        gameJoinVO.setGameRoomId(roomId);
        gameJoinVO.setUserId(userId);
        gameJoinService.leave(gameJoinVO);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponseDTO.of("사용자 강퇴"));
    }

}
