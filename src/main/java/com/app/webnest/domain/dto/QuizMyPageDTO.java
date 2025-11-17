package com.app.webnest.domain.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode( of = "id")
public class QuizMyPageDTO {
    private String quizTitle;
    private String quizDescription;
    private String quizCategory;
    private String quizExpectation;
    private Integer quizExp;
    private String quizDifficult;
    private String quizLanguage;
    private Long userId;
}
