package com.app.webnest.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "quizPersonalId")
public class QuizPersonalResponseDTO {
    private Long quizPersonalId;
    private Integer quizPersonalIsSolve;
    private Integer quizPersonalIsBookmark;
    private Long userId;
    private Long quizId;
}
