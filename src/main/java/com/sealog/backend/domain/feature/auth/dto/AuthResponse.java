package com.sealog.backend.domain.feature.auth.dto;

import com.sealog.backend.domain.feature.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 인증 관련 응답 DTO
 */
public class AuthResponse {

    /**
     * 토큰 및 사용자 프로필 요약 응답
     */
    @Getter
    @Builder
    public static class Token {
        private final String accessToken;
        private final String refreshToken;
        private final AuthProfile authProfile;

        public static Token of(String accessToken, String refreshToken, AuthProfile authProfile) {
            return Token.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .authProfile(authProfile)
                    .build();
        }
    }

    /**
     * 인증된 사용자 정보 응답
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

        public static AuthProfile of(Long id, String email, String name, String nickname, UserRole role, String profileImageUrl) {
            return AuthProfile.builder()
                    .id(id)
                    .email(email)
                    .name(name)
                    .nickname(nickname)
                    .role(role)
                    .profileImageUrl(profileImageUrl)
                    .build();
        }
    }
}
