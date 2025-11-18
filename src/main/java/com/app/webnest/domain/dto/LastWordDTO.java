package com.app.webnest.domain.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LastWordDTO {
    public String word;
    public String explanation;
    public String color;
    public boolean isFocus;
    public Long userId;
    public String userName;
    public Long gameRoomId;  // 게임방 ID
    public String previousWord;  // 이전 단어 (끝말잇기 체크용)
}
