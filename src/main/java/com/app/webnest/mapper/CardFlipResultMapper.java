package com.app.webnest.mapper;

import com.app.webnest.domain.dto.CardFlipResultDTO;
import com.app.webnest.domain.vo.CardFlipResultVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CardFlipResultMapper {

    /**
     * 카드 뒤집기 결과 저장
     */
    void insert(CardFlipResultVO cardFlipResultVO);

    /**
     * 카드 뒤집기 결과 업데이트
     */
    void update(CardFlipResultVO cardFlipResultVO);

    /**
     * 순위만 업데이트
     */
    void updateRank(CardFlipResultVO cardFlipResultVO);

    /**
     * 게임방 내 모든 결과 조회 (순위 포함, 사용자 정보 JOIN, 완료 시간 순 정렬)
     */
    List<CardFlipResultDTO> selectAllByGameRoomId(Long gameRoomId);

    /**
     * 특정 사용자의 특정 방 결과 조회
     */
    Optional<CardFlipResultVO> selectByUserIdAndGameRoomId(CardFlipResultVO cardFlipResultVO);

    /**
     * 게임방의 결과 개수 조회 (순위 계산용)
     */
    Integer countByGameRoomId(Long gameRoomId);

    /**
     * 게임방의 모든 결과 삭제
     */
    void deleteAllByGameRoomId(Long gameRoomId);
}

