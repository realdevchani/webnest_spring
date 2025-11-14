package com.app.webnest.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class TypingContentsDTO {
    private Long id;
    private String title;
    private String subject;
    private String type;
    private String language;
}
