package com.app.webnest.domain.vo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class CardFlipResultVO {
    private Long id;
    private Long userId;
    private Long gameRoomId;

    private Integer cardFlipResultFinishTime;  // 완료 시간 (초)
    private Integer cardFlipResultMatchedPairs; // 매칭된 쌍 수 (10이면 완료)
    private Integer cardFlipResultRankInRoom;  // 방 내 순위
    private LocalDateTime cardFlipResultCreateAt;

}

