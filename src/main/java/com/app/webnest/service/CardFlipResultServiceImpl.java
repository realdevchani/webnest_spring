package com.app.webnest.service;

import com.app.webnest.domain.dto.CardFlipResultDTO;
import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.dto.UserResponseDTO;
import com.app.webnest.domain.vo.CardFlipResultVO;
import com.app.webnest.repository.CardFlipResultDAO;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.GameRoomService;
import com.app.webnest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CardFlipResultServiceImpl implements CardFlipResultService {

    private final CardFlipResultDAO cardFlipResultDAO;
    private final UserService userService;
    private final GameJoinService gameJoinService;
    private final GameRoomService gameRoomService;

    @Override
    public CardFlipResultDTO save(CardFlipResultVO cardFlipResultVO) {
        Long userId = cardFlipResultVO.getUserId();
        Long gameRoomId = cardFlipResultVO.getGameRoomId();

        // 1단계: 결과 저장 (게임방이 사라지므로 항상 새로 저장)
        try {
            cardFlipResultDAO.save(cardFlipResultVO);
        } catch (Exception e) {
            log.error("결과 저장 중 예외 발생 - userId: {}, gameRoomId: {}, error: {}", 
                    userId, gameRoomId, e.getMessage(), e);
            throw e;
        }

        // 2단계: 순위 계산 및 업데이트
        try {
            updateAllRanks(gameRoomId);
        } catch (Exception e) {
            log.error("순위 계산 중 예외 발생 - gameRoomId: {}, error: {}", gameRoomId, e.getMessage(), e);
            // 순위 계산 실패해도 저장은 성공했으므로 계속 진행
        }

        // 3단계: 저장된 결과 반환 (삭제 전에 조회 - 중요!)
        CardFlipResultDTO savedResult = null;
        try {
            List<CardFlipResultDTO> allResults = cardFlipResultDAO.findAllByGameRoomId(gameRoomId);
            
            if (allResults == null) {
                log.error("결과 조회 실패 - allResults가 null. userId: {}, gameRoomId: {}", userId, gameRoomId);
                savedResult = null;
            } else if (allResults.isEmpty()) {
                log.warn("결과 조회 실패 - 결과가 없음. userId: {}, gameRoomId: {}, matchedPairs: {}", 
                        userId, gameRoomId, cardFlipResultVO.getCardFlipResultMatchedPairs());
                // 저장은 성공했지만 조회가 안 되는 경우, 저장한 VO를 기반으로 DTO 생성
                CardFlipResultVO searchVO = new CardFlipResultVO();
                searchVO.setUserId(userId);
                searchVO.setGameRoomId(gameRoomId);
                Optional<CardFlipResultVO> savedVO = cardFlipResultDAO.findByUserIdAndGameRoomId(searchVO);
                
                if (savedVO.isPresent()) {
                    savedResult = buildDTOFromVO(savedVO.get(), gameRoomId);
                } else {
                    log.error("직접 조회도 실패 - 저장된 결과를 찾을 수 없음. userId: {}, gameRoomId: {}", userId, gameRoomId);
                    savedResult = null;
                }
            } else {
                // 현재 사용자의 결과 찾기
                for (CardFlipResultDTO result : allResults) {
                    if (result.getUserId() != null && result.getUserId().equals(userId)) {
                        savedResult = result;
                        break;
                    }
                }
                
                if (savedResult == null) {
                    log.warn("저장된 결과를 찾을 수 없음 - userId: {}, gameRoomId: {}, 조회된 결과 수: {}, 결과 userId 목록: {}", 
                            userId, gameRoomId, allResults.size(), 
                            allResults.stream().map(r -> r.getUserId()).toList());
                }
            }
        } catch (Exception e) {
            log.error("결과 조회 중 예외 발생 - userId: {}, gameRoomId: {}, error: {}", 
                    userId, gameRoomId, e.getMessage(), e);
            savedResult = null;
        }

        // 4단계: 게임 완료 확인 및 처리 (결과 반환 후에 처리)
        Integer matchedPairs = cardFlipResultVO.getCardFlipResultMatchedPairs();
        if (matchedPairs != null && matchedPairs == 10) {
            // 게임 완료 처리 (경험치 지급 등, 삭제는 비동기로 처리)
            handleGameCompletionWithoutDelete(userId, gameRoomId);
        }

        return savedResult;
    }

    /**
     * 모든 플레이어의 순위를 계산하고 업데이트
     */
    private void updateAllRanks(Long gameRoomId) {
        // 게임방의 모든 결과 가져오기 (finishTime 순으로 정렬됨)
        List<CardFlipResultDTO> allResults = cardFlipResultDAO.findAllByGameRoomId(gameRoomId);

        if (allResults == null || allResults.isEmpty()) {
            log.warn("순위 계산 실패 - 결과가 없음. gameRoomId: {}", gameRoomId);
            return;
        }

        // 순위 계산 (1등부터 시작)
        int rank = 1;
        for (CardFlipResultDTO result : allResults) {
            if (result.getUserId() == null) {
                log.warn("순위 업데이트 건너뜀 - userId가 null. result: {}", result);
                continue;
            }
            
            // 순위 업데이트
            CardFlipResultVO updateVO = new CardFlipResultVO();
            updateVO.setUserId(result.getUserId());
            updateVO.setGameRoomId(gameRoomId);
            updateVO.setCardFlipResultRankInRoom(rank);

            try {
                cardFlipResultDAO.updateRank(updateVO);
            } catch (Exception e) {
                log.error("순위 업데이트 실패 - userId: {}, rank: {}, error: {}", 
                        result.getUserId(), rank, e.getMessage(), e);
            }
            rank++;
        }
    }

    /**
     * 게임 완료 처리 (경험치 지급만, 삭제는 하지 않음)
     */
    private void handleGameCompletionWithoutDelete(Long userId, Long gameRoomId) {
        // 1. 순위 확인
        List<CardFlipResultDTO> allResults = cardFlipResultDAO.findAllByGameRoomId(gameRoomId);
        int myRank = 999;

        for (CardFlipResultDTO result : allResults) {
            if (result.getUserId() != null && result.getUserId().equals(userId)) {
                if (result.getCardFlipResultRankInRoom() != null) {
                    myRank = result.getCardFlipResultRankInRoom();
                }
                break;
            }
        }

        // 2. 경험치 계산
        int expGain = 50; // 기본 경험치
        if (myRank == 1) {
            expGain = 20;
        } else if (myRank == 2) {
            expGain = 15;
        } else if (myRank == 3) {
            expGain = 10;
        }

        // 3. 유저 경험치 업데이트
        userService.gainExp(userId, expGain);

        // 4. 모든 플레이어 완료 확인 및 기록 삭제
        List<GameJoinDTO> players = gameJoinService.getPlayers(gameRoomId);
        int totalPlayers = players.size();
        int completedPlayers = allResults.size();

        // 모든 플레이어가 완료했으면 기록 삭제 (비동기로 처리하거나 나중에 삭제)
        if (completedPlayers >= totalPlayers) {
            // 결과 반환 후 삭제하기 위해 별도 스레드에서 처리
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 1초 대기 후 삭제
                    cardFlipResultDAO.deleteAllByGameRoomId(gameRoomId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("기록 삭제 중 인터럽트 발생", e);
                } catch (Exception e) {
                    log.error("기록 삭제 중 오류 발생", e);
                }
            }).start();
        }
    }

    @Override
    public List<CardFlipResultDTO> findAllByGameRoomId(Long gameRoomId) {
        return cardFlipResultDAO.findAllByGameRoomId(gameRoomId);
    }

    @Override
    public void deleteAllByGameRoomId(Long gameRoomId) {
        cardFlipResultDAO.deleteAllByGameRoomId(gameRoomId);
    }

    /**
     * VO를 기반으로 DTO 생성 (사용자 정보 및 게임방 정보 포함)
     */
    private CardFlipResultDTO buildDTOFromVO(CardFlipResultVO vo, Long gameRoomId) {
        try {
            CardFlipResultDTO dto = new CardFlipResultDTO();
            dto.setId(vo.getId());
            dto.setUserId(vo.getUserId());
            dto.setGameRoomId(vo.getGameRoomId());
            dto.setCardFlipResultFinishTime(vo.getCardFlipResultFinishTime());
            dto.setCardFlipResultMatchedPairs(vo.getCardFlipResultMatchedPairs());
            dto.setCardFlipResultRankInRoom(vo.getCardFlipResultRankInRoom());
            dto.setCardFlipResultCreateAt(vo.getCardFlipResultCreateAt());

            // 사용자 정보 조회
            try {
                UserResponseDTO user = userService.getUserById(vo.getUserId());
                dto.setUserNickname(user.getUserNickname());
                dto.setUserThumbnailName(user.getUserThumbnailName());
                dto.setUserThumbnailUrl(user.getUserThumbnailUrl());
                dto.setUserLevel(user.getUserLevel());
                dto.setUserExp(user.getUserExp());
            } catch (Exception e) {
                log.warn("사용자 정보 조회 실패 - userId: {}, error: {}", vo.getUserId(), e.getMessage());
            }

            // 게임방 정보 조회
            try {
                var gameRoom = gameRoomService.getRoom(gameRoomId);
                if (gameRoom != null) {
                    dto.setGameRoomMaxPlayer(gameRoom.getGameRoomMaxPlayer());
                }
            } catch (Exception e) {
                log.warn("게임방 정보 조회 실패 - gameRoomId: {}, error: {}", gameRoomId, e.getMessage());
            }

            return dto;
        } catch (Exception e) {
            log.error("DTO 생성 실패 - vo: {}, error: {}", vo, e.getMessage(), e);
            return null;
        }
    }
}