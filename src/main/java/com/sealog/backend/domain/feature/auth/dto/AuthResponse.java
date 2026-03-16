package com.sealog.backend.domain.feature.auth.dto;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "인증 사용자 프로필")
    public static class AuthProfile {

        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "닉네임", example = "seadev")
        private String nickname;

        @Schema(description = "권한 (USER / ADMIN)", example = "USER")
        private UserRole role;

        @Schema(description = "프로필 이미지 URL (없으면 null)", example = "https://cdn.example.com/profile/abc.jpg")
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
