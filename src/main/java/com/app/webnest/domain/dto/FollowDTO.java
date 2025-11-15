package com.app.webnest.domain.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
public class FollowDTO {
    private Long id;
    private Long userId;
    private Long followerId; // 확인 필요  -- 팔로잉 한 사용자
    private Date followCreateAt;  //create at으로 동일하게 erd를 고칩시다
    
    // 팔로잉/팔로워 유저 정보
    private String userNickname;
    private String userThumbnailUrl;
    private Integer userLevel;
    private String userEmail;

    private String presenceStatus;
    
}
