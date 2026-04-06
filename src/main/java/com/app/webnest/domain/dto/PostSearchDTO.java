package com.app.webnest.domain.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@ToString
public class PostSearchDTO {
    //    TBP.ID, TBP.POST_TITLE, TBP.POST_CONTENT,
//    TBP.POST_CREATE_AT, TBP.POST_VIEW_COUNT, TBP.POST_TYPE, TBP.USER_ID,
//    TBU.USER_LEVEL, TBU.USER_THUMBNAIL_URL, TBU.USER_NICKNAME,
//    COUNT(TBC.ID) AS COMMENT_COUNT
    private Long id;
    private String postTitle; // erd 추가
    private String postContent;
    private Date postCreateAt;
    private Integer postViewCount;
    private String postType;
    private Long userId;
    private Integer userLevel;
    private String userThumbnailUrl;
    private String userNickname;
    private Integer commentCount;
    private Integer score;
}
