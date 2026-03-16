package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.enums.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class UserResponse {

    @Getter
    @Builder
    @Schema(description = "사용자 공개 프로필 정보")
    public static class UserProfile {

        @Schema(description = "닉네임", example = "seadev")
        private String nickname;

        @Schema(description = "포지션 (없으면 null)", example = "Java/Spring 백엔드 개발자")
        private String position;

        @Schema(description = "프로필 이미지 URL (없으면 null)", example = "https://cdn.example.com/profile/abc.jpg")
        private String profileImageUrl;

        @Schema(description = "소개 (없으면 null)", example = "Java/Spring 기반 백엔드 개발자입니다.")
        private String about;

        @Schema(description = "소셜 링크 목록")
        private List<SocialLinkItem> socialLinks;

        public static UserProfile of(
                String nickname, String position,
                String profileImageUrl, String about,
                List<SocialLinkItem> socialLinks
        ) {
            return UserProfile.builder()
                    .nickname(nickname).position(position)
                    .profileImageUrl(profileImageUrl).about(about)
                    .socialLinks(socialLinks)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "사용자 소셜 링크 정보 (공개용)")
    public static class SocialLinkItem {

        @Schema(description = "소셜 타입 (GITHUB / NOTION / PORTFOLIO / LINKEDIN / YOUTUBE / INSTAGRAM)", example = "GITHUB")
        private SocialType socialType;

        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;

        public static SocialLinkItem of(SocialType socialType, String url) {
            return SocialLinkItem.builder().socialType(socialType).url(url).build();
        }
    }
}
