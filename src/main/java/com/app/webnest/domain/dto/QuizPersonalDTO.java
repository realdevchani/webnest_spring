package com.app.webnest.domain.dto;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class QuizPersonalDTO {
    private Long id;
    //erd 확인 fk키
    private String quizTitle;
    private String quizDescription;
    private String quizDifficult;
    private String quizLanguage;
    private String quizCategory; // 더미 데이터 확인 후 넣을지 확인
    private Integer quizExp; // erd 추가
    private String quizExpectation;
    private Long quizPersonalId;
    private Integer quizPersonalIsSolve;
    private Integer quizPersonalIsBookmark;
    private Long userId;
    private Long quizId;
}