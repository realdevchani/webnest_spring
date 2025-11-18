package com.app.webnest.domain.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class TypingRecordVO {
    private Long id;
    private Double typingRecordTypist;
    private Double typingRecordAccuracy;
    private String typingRecordTime;
    private Long userId;
    private Long typingContentsId;
}



