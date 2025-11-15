package com.app.webnest.domain.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class GameRoomDTO {
    private Long id; // erd pk키 확인
    private String gameRoomTitle;
    private boolean gameRoomIsTeam;
    private String gameRoomType; // erd 게임 유형 중복
    private boolean gameRoomIsOpen;
    private Integer gameRoomCurrentPlayer;
    private Integer gameRoomMaxPlayer;
    private boolean gameRoomIsStart;
    private String gameRoomPassKey;
    private LocalDateTime gameRoomCreateAt;
    private String gameRoomLanguage;

    // 게임방 목록을 조회할 때 참여한 유저를 모두 조회한다.
    private List<GameJoinDTO> players;

//    게임방 목록 조회할 때 내가팔로우 하는 사람들도 나와야됨
    private List<FollowDTO> followers;

    // 현재 로그인한 사용자의 연승 횟수
    private Integer winCount;
}
