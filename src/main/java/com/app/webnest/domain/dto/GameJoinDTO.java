package com.app.webnest.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class GameJoinDTO {
    private Long id;
    private Long userId;
    private Long gameRoomId; // 이알디 확인
    private boolean gameJoinIsHost;
    private String gameJoinTeamcolor;
    private LocalDateTime gameJoinCreateAt;
    private String userName;
    private String userBirthday;
    private String userEmail;
    private String userPhone;
    private String userThumbnailName;
    private String userThumbnailURL;
    private String userNickname;
    private Integer userLevel;
    private Integer userExp;
}
