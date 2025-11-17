package com.app.webnest.service;

import com.app.webnest.domain.dto.CardFlipResultDTO;
import com.app.webnest.domain.vo.CardFlipResultVO;

import java.util.List;

public interface CardFlipResultService {

    // 카드 뒤집기 결과 저장 (순위 계산 포함)
    public CardFlipResultDTO save(CardFlipResultVO cardFlipResultVO);

    // 게임방 내 모든 결과 조회 (순위 포함, 사용자 정보 JOIN)
    public List<CardFlipResultDTO> findAllByGameRoomId(Long gameRoomId);

    // 게임방의 모든 결과 삭제
    public void deleteAllByGameRoomId(Long gameRoomId);
}

