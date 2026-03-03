package com.sealog.backend.domain.feature.auth.dto;

import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

public class AuthResponse {
    /**
     * 사용자 정보 응답
     */
    @Getter
    @Builder
    public static class Profile {

        private Long id;
        private String email;
        private String name;
        private String nickname;
        private UserRole role;
        private String profileImagePath;

        public static AuthResponse.Profile from(User user) {
            return AuthResponse.Profile.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .profileImagePath(user.getProfileImagePath())
                    .build();
        }
    }
}