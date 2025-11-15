package com.app.webnest.domain.vo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class GameRoomVO {
    private Long id; // erd pk키 확인
    private String gameRoomTitle;
    private Integer gameRoomIsTeam;
    private String gameRoomType; // erd 게임 유형 중복
    private Integer gameRoomMaxPlayer;
    private Integer gameRoomIsStart;
    private Integer gameRoomIsOpen;
    private String gameRoomPassKey;
    private LocalDateTime gameRoomCreatedAt;
    private String gameRoomLanguage;
}