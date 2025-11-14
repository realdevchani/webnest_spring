package com.app.webnest.domain.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class TypingContentsVO {
    private Long id;
    private String typingContentsTitle;
    private String typingContentsSubject;
    private String typingContentsType;
    private String typingContentsLanguage;

}

