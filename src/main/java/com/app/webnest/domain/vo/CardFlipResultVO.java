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
    private Integer finishTime;  // 완료 시간 (초)
    private Integer matchedPairs; // 매칭된 쌍 수 (10이면 완료)
    private Integer score;  // 획득 점수
    private Integer rankInRoom;  // 방 내 순위
    private LocalDateTime createdAt;
}

