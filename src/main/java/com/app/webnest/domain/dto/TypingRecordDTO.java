package com.app.webnest.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class TypingRecordDTO {
    private Long id;
    private Double typingRecordTypist;
    private Double typingRecordAccuracy;
    private String typingRecordTime;
    private Long userId;
    private Long typingContentsId;

}
