package com.app.webnest.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 카드 뒤집기 게임 결과 DTO
 * API 응답에 사용 (사용자 정보 포함)
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class CardFlipResultDTO {
    
    // VO와 동일한 필드
    private Long id;
    private Long userId;
    private Long gameRoomId;
    private Integer cardFlipResultFinishTime;  // 완료 시간 (초)
    private Integer cardFlipResultMatchedPairs; // 매칭된 쌍 수 (10이면 완료)
    private Integer cardFlipResultRankInRoom;  // 방 내 순위
    private LocalDateTime cardFlipResultCreateAt;
    
    // 사용자 정보 (JOIN으로 가져옴)
    private String userNickname;
    private String userThumbnailName;
    private String userThumbnailUrl;
    private Integer userLevel;
    private Integer userExp;
    private Integer gameRoomMaxPlayer;  // 게임방 최대 인원 수
}
