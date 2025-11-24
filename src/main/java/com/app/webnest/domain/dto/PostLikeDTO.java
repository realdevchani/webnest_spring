package com.app.webnest.domain.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class PostLikeDTO {
    private Long id;
    private Long userId;
    private Long postId;

    private String postContent; //
    private String postTitle;
    private Date postCreateAt;
    private Integer postViewCount;
    private String postType;
    
    // 작성자 정보
    private String userNickname;
    private String userThumbnailUrl;
    private String userThumbnailName;
    private Integer commentCount;
}
