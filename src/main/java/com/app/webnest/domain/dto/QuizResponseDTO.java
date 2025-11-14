package com.app.webnest.domain.dto;

import lombok.*;

import java.util.Date;

@Getter @Setter
@ToString
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class QuizResponseDTO {
    private Long quizId;
    private Long userId;
    private String quizSubmitCode;
    private String quizSubmitResult;
    private Date quizSubmitCreateAt;
    private String className;
}
