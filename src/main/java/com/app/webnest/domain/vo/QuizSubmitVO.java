package com.app.webnest.domain.vo;

import lombok.*;

import java.util.Date;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class QuizSubmitVO {
    private Long id;
    private String quizSubmitCode;
    private Date quizSubmitCreateAt; // erd 제출시간 이름 바꾸기
    private Integer quizSubmitResult;
    private String quizSubmitError; // erd이름 쓰기
    private Long userId;
    private Long quizId;
}