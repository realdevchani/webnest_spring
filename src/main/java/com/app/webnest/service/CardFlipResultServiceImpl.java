package com.app.webnest.service;

import com.app.webnest.domain.dto.CardFlipResultDTO;
import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.CardFlipResultVO;
import com.app.webnest.repository.CardFlipResultDAO;
import com.app.webnest.service.GameJoinService;
import com.app.webnest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
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

    @Override
    public CardFlipResultDTO save(CardFlipResultVO cardFlipResultVO) {
        log.info("카드 뒤집기 결과 저장 시작 - userId: {}, gameRoomId: {}, finishTime: {}, matchedPairs: {}",
                cardFlipResultVO.getUserId(), cardFlipResultVO.getGameRoomId(),
                cardFlipResultVO.getFinishTime(), cardFlipResultVO.getMatchedPairs());

        // userId와 gameRoomId를 final 변수로 저장 (람다 표현식에서 사용하기 위해)
        final Long userId = cardFlipResultVO.getUserId();
        final Long gameRoomId = cardFlipResultVO.getGameRoomId();

        // 1. 이미 기록이 있는지 확인 (같은 사용자가 같은 방에서 이미 완료한 경우)
        Optional<CardFlipResultVO> existingResult = cardFlipResultDAO.findByUserIdAndGameRoomId(cardFlipResultVO);

        if (existingResult.isPresent()) {
            // 이미 기록이 있으면 업데이트 (더 빠른 시간으로 재도전한 경우)
            log.info("기존 기록 발견, 업데이트 - userId: {}, gameRoomId: {}", 
                    cardFlipResultVO.getUserId(), cardFlipResultVO.getGameRoomId());
            // 기존 기록의 시간과 비교하여 더 빠른 시간일 때만 업데이트
            CardFlipResultVO existing = existingResult.get();
            if (cardFlipResultVO.getFinishTime() < existing.getFinishTime()) {
                // 순위는 다시 계산해야 하므로 일단 저장하고 전체 조회 후 순위 재계산
                // 하지만 간단하게 기존 순위 유지하거나 전체 조회 후 재계산 필요
                cardFlipResultDAO.update(cardFlipResultVO);
            } else {
                log.info("기존 기록이 더 빠름, 업데이트 안함 - 기존: {}초, 새: {}초",
                        existing.getFinishTime(), cardFlipResultVO.getFinishTime());
                // 기존 기록이 더 빠르면 업데이트하지 않음
                cardFlipResultVO = existing;
            }
        } else {
            // 기록이 없으면 새로 저장
            cardFlipResultDAO.save(cardFlipResultVO);
            log.info("새로운 기록 저장 완료 - userId: {}, gameRoomId: {}", 
                    cardFlipResultVO.getUserId(), cardFlipResultVO.getGameRoomId());
        }

        // 2. 순위 계산을 위해 전체 결과 조회
        List<CardFlipResultDTO> allResults = cardFlipResultDAO.findAllByGameRoomId(gameRoomId);
        
        // 3. 순위 재계산 및 업데이트 (필요시)
        // 현재는 조회 시 ROW_NUMBER()로 순위 계산하므로 별도 업데이트 불필요
        // 하지만 rankInRoom을 저장하려면 업데이트 필요

        // 4. 게임 완료 시 경험치 추가 (10쌍 모두 매칭 완료한 경우)
        if (cardFlipResultVO.getMatchedPairs() != null && cardFlipResultVO.getMatchedPairs() == 10) {
            log.info("게임 완료 - 경험치 추가 시작 - userId: {}, gameRoomId: {}", 
                    userId, gameRoomId);
            
            // 게임방의 플레이어 정보 조회
            List<GameJoinDTO> players = gameJoinService.getPlayers(gameRoomId);
            
            // 완료한 사용자 찾기
            GameJoinDTO completedPlayer = players.stream()
                    .filter(p -> p.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
            
            if (completedPlayer != null) {
                // 순위에 따라 경험치 차등 지급
                int rank = allResults.stream()
                        .filter(r -> r.getUserId().equals(userId))
                        .findFirst()
                        .map(r -> r.getRankInRoom() != null ? r.getRankInRoom() : 999)
                        .orElse(999);
                
                // 순위별 경험치: 1등 200, 2등 150, 3등 100, 그 외 50
                int expGain = 50; // 기본 경험치
                if (rank == 1) {
                    expGain = 200;
                } else if (rank == 2) {
                    expGain = 150;
                } else if (rank == 3) {
                    expGain = 100;
                }
                
                // 경험치 추가 (기존 경험치 + 획득 경험치)
                int currentExp = completedPlayer.getUserExp() != null ? completedPlayer.getUserExp() : 0;
                int newExp = currentExp + expGain;
                completedPlayer.setUserExp(newExp);
                
                // userMapper.xml의 updateUserEXPByGameResult가 #{id}와 #{userEXP}를 사용하므로 설정
                // 1. id를 userId로 설정 (TBL_USER의 ID는 userId)
                completedPlayer.setId(completedPlayer.getUserId());
                
                // 2. userMapper.xml이 #{userEXP}를 사용하므로 getUserEXP() 메서드가 필요
                // GameJoinDTO에 userEXP 필드를 추가하거나 getUserEXP() 메서드를 추가해야 함
                // 임시로 userExp를 사용하도록 시도 (MyBatis가 자동으로 매핑하지 못할 수 있음)
                userService.modifyUserEXPByGameResult(completedPlayer);
                
                log.info("경험치 추가 완료 - userId: {}, rank: {}, expGain: {}, totalExp: {}", 
                        userId, rank, expGain, completedPlayer.getUserExp());
                
                // 5. 저장된 결과 조회 (삭제 전에 조회해야 함)
                CardFlipResultDTO savedResultDTO = null;
                CardFlipResultVO queryVO = new CardFlipResultVO();
                queryVO.setUserId(userId);
                queryVO.setGameRoomId(gameRoomId);
                Optional<CardFlipResultVO> savedResult = cardFlipResultDAO.findByUserIdAndGameRoomId(queryVO);
                if (savedResult.isPresent()) {
                    // DTO로 변환하여 반환 (사용자 정보는 전체 조회로 가져와야 함)
                    List<CardFlipResultDTO> results = cardFlipResultDAO.findAllByGameRoomId(gameRoomId);
                    savedResultDTO = results.stream()
                            .filter(r -> r.getUserId().equals(userId))
                            .findFirst()
                            .orElse(null);
                }
                
                // 6. 게임 종료 확인: 모든 플레이어가 게임을 완료했는지 확인
                int totalPlayers = players.size();
                int completedPlayers = allResults.size(); // 완료한 플레이어 수 (matchedPairs == 10인 플레이어)
                
                log.info("게임 완료 상태 확인 - gameRoomId: {}, totalPlayers: {}, completedPlayers: {}", 
                        gameRoomId, totalPlayers, completedPlayers);
                
                // 모든 플레이어가 게임을 완료했으면 기록 삭제
                if (completedPlayers >= totalPlayers) {
                    log.info("모든 플레이어 게임 완료 - 게임방 기록 삭제 시작 - gameRoomId: {}", 
                            gameRoomId);
                    cardFlipResultDAO.deleteAllByGameRoomId(gameRoomId);
                    log.info("게임방 기록 삭제 완료 - gameRoomId: {}", gameRoomId);
                }
                
                // 저장된 결과 반환 (삭제 전에 조회한 결과)
                return savedResultDTO;
            } else {
                log.warn("완료한 플레이어를 찾을 수 없음 - userId: {}, gameRoomId: {}", 
                        userId, gameRoomId);
            }
        }

        // 경험치를 지급하지 않은 경우에도 저장된 결과 조회하여 반환
        CardFlipResultVO queryVO = new CardFlipResultVO();
        queryVO.setUserId(userId);
        queryVO.setGameRoomId(gameRoomId);
        Optional<CardFlipResultVO> savedResult = cardFlipResultDAO.findByUserIdAndGameRoomId(queryVO);
        if (savedResult.isPresent()) {
            // DTO로 변환하여 반환 (사용자 정보는 전체 조회로 가져와야 함)
            List<CardFlipResultDTO> results = cardFlipResultDAO.findAllByGameRoomId(gameRoomId);
            return results.stream()
                    .filter(r -> r.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    @Override
    public List<CardFlipResultDTO> findAllByGameRoomId(Long gameRoomId) {
        log.info("카드 뒤집기 결과 조회 - gameRoomId: {}", gameRoomId);
        return cardFlipResultDAO.findAllByGameRoomId(gameRoomId);
    }

    @Override
    public void deleteAllByGameRoomId(Long gameRoomId) {
        log.info("카드 뒤집기 결과 삭제 - gameRoomId: {}", gameRoomId);
        cardFlipResultDAO.deleteAllByGameRoomId(gameRoomId);
    }
}

