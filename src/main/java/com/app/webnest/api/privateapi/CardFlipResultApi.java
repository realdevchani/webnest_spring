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
     * RequestBody: { userId: Long, finishTime: Integer, matchedPairs: Integer, score: Integer }
     */
    @PostMapping("/{gameRoomId}/cardflip/finish")
    public ResponseEntity<ApiResponseDTO<CardFlipResultDTO>> finishGame(
            @PathVariable Long gameRoomId,
            @RequestBody Map<String, Object> request) {
        
        log.info("카드 뒤집기 게임 완료 요청 - gameRoomId: {}, request: {}", gameRoomId, request);
        
        try {
            // RequestBody에서 데이터 추출
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer finishTime = Integer.valueOf(request.get("finishTime").toString());
            Integer matchedPairs = Integer.valueOf(request.get("matchedPairs").toString());
            Integer score = request.get("score") != null ? Integer.valueOf(request.get("score").toString()) : null;

            // CardFlipResultVO 생성
            CardFlipResultVO cardFlipResultVO = new CardFlipResultVO();
            cardFlipResultVO.setUserId(userId);
            cardFlipResultVO.setGameRoomId(gameRoomId);
            cardFlipResultVO.setFinishTime(finishTime);
            cardFlipResultVO.setMatchedPairs(matchedPairs);
            cardFlipResultVO.setScore(score);
            // rankInRoom은 Service에서 계산

            // 결과 저장 (순위 계산 포함)
            CardFlipResultDTO savedResult = cardFlipResultService.save(cardFlipResultVO);

            if (savedResult == null) {
                log.error("카드 뒤집기 결과 저장 실패 - gameRoomId: {}, userId: {}", gameRoomId, userId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDTO.of("결과 저장에 실패했습니다.", null));
            }

            log.info("카드 뒤집기 결과 저장 성공 - gameRoomId: {}, userId: {}, finishTime: {}, rank: {}",
                    gameRoomId, userId, finishTime, savedResult.getRankInRoom());

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
        
        log.info("카드 뒤집기 결과 조회 요청 - gameRoomId: {}", gameRoomId);
        
        try {
            List<CardFlipResultDTO> results = cardFlipResultService.findAllByGameRoomId(gameRoomId);
            
            log.info("카드 뒤집기 결과 조회 성공 - gameRoomId: {}, 결과 개수: {}", gameRoomId, results.size());
            
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

