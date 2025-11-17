package com.app.webnest.repository;

import com.app.webnest.domain.dto.CardFlipResultDTO;
import com.app.webnest.domain.vo.CardFlipResultVO;
import com.app.webnest.mapper.CardFlipResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CardFlipResultDAO {

    private final CardFlipResultMapper cardFlipResultMapper;

    // 카드 뒤집기 결과 저장
    public void save(CardFlipResultVO cardFlipResultVO) {
        cardFlipResultMapper.insert(cardFlipResultVO);
    }

    // 카드 뒤집기 결과 업데이트
    public void update(CardFlipResultVO cardFlipResultVO) {
        cardFlipResultMapper.update(cardFlipResultVO);
    }

    // 게임방 내 모든 결과 조회 (순위 포함, 사용자 정보 JOIN)
    public List<CardFlipResultDTO> findAllByGameRoomId(Long gameRoomId) {
        return cardFlipResultMapper.selectAllByGameRoomId(gameRoomId);
    }

    // 특정 사용자의 특정 방 결과 조회
    public Optional<CardFlipResultVO> findByUserIdAndGameRoomId(CardFlipResultVO cardFlipResultVO) {
        return cardFlipResultMapper.selectByUserIdAndGameRoomId(cardFlipResultVO);
    }

    // 게임방의 결과 개수 조회 (순위 계산용)
    public Integer countByGameRoomId(Long gameRoomId) {
        return cardFlipResultMapper.countByGameRoomId(gameRoomId);
    }

    // 게임방의 모든 결과 삭제
    public void deleteAllByGameRoomId(Long gameRoomId) {
        cardFlipResultMapper.deleteAllByGameRoomId(gameRoomId);
    }
}

