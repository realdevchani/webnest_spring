package com.app.webnest.mapper;

import com.app.webnest.domain.dto.CardFlipResultDTO;
import com.app.webnest.domain.vo.CardFlipResultVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CardFlipResultMapper {

    // 카드 뒤집기 결과 저장
    public void insert(CardFlipResultVO cardFlipResultVO);

    // 카드 뒤집기 결과 업데이트 (같은 사용자가 같은 방에서 다시 완료한 경우)
    public void update(CardFlipResultVO cardFlipResultVO);

    // 게임방 내 모든 결과 조회 (순위 포함, 사용자 정보 JOIN)
    public List<CardFlipResultDTO> selectAllByGameRoomId(Long gameRoomId);

    // 특정 사용자의 특정 방 결과 조회
    public Optional<CardFlipResultVO> selectByUserIdAndGameRoomId(CardFlipResultVO cardFlipResultVO);

    // 게임방의 결과 개수 조회 (순위 계산용)
    public Integer countByGameRoomId(Long gameRoomId);

    // 게임방의 모든 결과 삭제 (게임방 삭제 시)
    public void deleteAllByGameRoomId(Long gameRoomId);
}

