package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

public class UserResponse {

    /**
     * 사용자 정보 응답 (내 정보)
     */
    @Getter
    @Builder
    public static class MyProfile {

        private Long id;
        private String email;
        private String name;
        private String nickname;
        private UserRole role;
        private String position;
        private String about;
        private String profileImageUrl;

        public static MyProfile of(User user, String profileImageUrl) {
            return MyProfile.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .position(user.getPosition())
                    .about(user.getAbout())
                    .profileImageUrl(profileImageUrl)
                    .build();
        }
    }

    /**
     * [외부 노출용] 블로그 방문자가 보는 프로필
     */
    @Getter
    @Builder
    public static class PublicProfile {

        private String nickname;
        private String profileImageUrl;
        private String position;
        private String about;

        public static PublicProfile of(User user, String profileImageUrl) {
            return PublicProfile.builder()
                    .nickname(user.getNickname())
                    .profileImageUrl(profileImageUrl)
                    .position(user.getPosition())
                    .about(user.getAbout())
                    .build();
        }
    }
}
