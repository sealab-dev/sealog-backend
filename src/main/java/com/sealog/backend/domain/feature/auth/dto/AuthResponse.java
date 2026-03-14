package com.sealog.backend.domain.feature.auth.dto;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

public class AuthResponse {

    @Getter
    @Builder
    public static class Token {

        private final String accessToken;
        private final String refreshToken;
        private final AuthProfile authProfile;
    }

    /**
     * 사용자 정보 응답
     */
    @Getter
    @Builder
    public static class AuthProfile {

        private Long id;
        private String email;
        private String name;
        private String nickname;
        private UserRole role;
        private String profileImageUrl;

        public static AuthProfile from(User user, String profileImageUrl) {
            return AuthProfile.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .profileImageUrl(profileImageUrl)
                    .build();
        }
    }
}