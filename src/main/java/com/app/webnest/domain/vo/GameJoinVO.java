package com.app.webnest.domain.vo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class GameJoinVO {
    private Long id;
    private Long userId;
    private Long gameRoomId;
    private Integer gameJoinIsHost;
    private String gameJoinTeamcolor;
    private Integer gameJoinMyturn;
    private String gameJoinProfileText;
    private LocalDateTime gameJoinCreateAt;
    private Integer gameJoinPosition;
    private Integer gameJoinIsReady;

    public GameJoinVO(ChatMessageVO chatMessageVO) {
        this.gameRoomId = chatMessageVO.getGameRoomId();
        this.userId = chatMessageVO.getUserSenderId();
        this.gameJoinTeamcolor = chatMessageVO.getUserSenderTeamcolor();
    }

}
