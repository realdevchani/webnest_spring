package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.CardFlipResultDTO;
import com.app.webnest.domain.vo.CardFlipResultVO;
import com.app.webnest.service.CardFlipResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/private/game-rooms")
public class CardFlipResultApi {

    private final CardFlipResultService cardFlipResultService;

    /**
     * 카드 뒤집기 게임 완료 결과 저장
     * POST /private/game-rooms/{gameRoomId}/cardflip/finish
     * RequestBody: { userId: Long, finishTime: Integer, matchedPairs: Integer }
     */
    @PostMapping("/{gameRoomId}/cardflip/finish")
    public ResponseEntity<ApiResponseDTO<CardFlipResultDTO>> finishGame(
            @PathVariable Long gameRoomId,
            @RequestBody Map<String, Object> request) {

        try {
            // RequestBody에서 데이터 추출
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer finishTime = Integer.valueOf(request.get("finishTime").toString());
            Integer matchedPairs = Integer.valueOf(request.get("matchedPairs").toString());

            // CardFlipResultVO 생성
            CardFlipResultVO cardFlipResultVO = new CardFlipResultVO();
            cardFlipResultVO.setUserId(userId);
            cardFlipResultVO.setGameRoomId(gameRoomId);
            cardFlipResultVO.setCardFlipResultFinishTime(finishTime);
            cardFlipResultVO.setCardFlipResultMatchedPairs(matchedPairs);
            // rankInRoom은 Service에서 계산

            // 결과 저장 (순위 계산 포함)
            CardFlipResultDTO savedResult = null;
            try {
                savedResult = cardFlipResultService.save(cardFlipResultVO);
            } catch (Exception serviceException) {
                log.error("카드 뒤집기 결과 저장 중 서비스 예외 발생 - gameRoomId: {}, userId: {}, error: {}",
                        gameRoomId, userId, serviceException.getMessage(), serviceException);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDTO.of("결과 저장 중 오류가 발생했습니다: " + serviceException.getMessage(), null));
            }

            if (savedResult == null) {
                log.error("카드 뒤집기 결과 저장 실패 - gameRoomId: {}, userId: {}, finishTime: {}, matchedPairs: {}",
                        gameRoomId, userId, finishTime, matchedPairs);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDTO.of("결과 저장에 실패했습니다.", null));
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.of("카드 뒤집기 게임 완료 결과 저장 성공", savedResult));

        } catch (Exception e) {
            log.error("카드 뒤집기 게임 완료 처리 중 오류 발생 - gameRoomId: {}, error: {}",
                    gameRoomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.of("결과 저장 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    /**
     * 게임방 내 모든 카드 뒤집기 결과 조회 (순위 포함)
     * GET /private/game-rooms/{gameRoomId}/cardflip/results
     */
    @GetMapping("/{gameRoomId}/cardflip/results")
    public ResponseEntity<ApiResponseDTO<List<CardFlipResultDTO>>> getResults(
            @PathVariable Long gameRoomId) {

        try {
            List<CardFlipResultDTO> results = cardFlipResultService.findAllByGameRoomId(gameRoomId);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDTO.of("카드 뒤집기 결과 조회 성공", results));

        } catch (Exception e) {
            log.error("카드 뒤집기 결과 조회 중 오류 발생 - gameRoomId: {}, error: {}",
                    gameRoomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.of("결과 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
}
