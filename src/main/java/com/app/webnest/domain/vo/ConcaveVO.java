package com.app.webnest.domain.vo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode( of = "id")
public class ConcaveVO {
    private int concaveRow;
    private int concaveCol;
    private Long userId;
    private Long gameRoomId;
}
