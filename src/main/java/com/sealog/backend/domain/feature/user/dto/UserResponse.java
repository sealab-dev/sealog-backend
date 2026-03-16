package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.enums.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class UserResponse {

    @Getter
    @Builder
    @Schema(description = "사용자 프로필 정보 (공개용)")
    public static class UserProfile {
        private String nickname;
        private String position;
        private String profileImageUrl;
        private String about;
        private List<SocialLinkItem> socialLinks;

        public static UserProfile of(
                String nickname,
                String position,
                String profileImageUrl,
                String about,
                List<SocialLinkItem> socialLinks
        ) {
            return UserProfile.builder()
                    .nickname(nickname)
                    .position(position)
                    .profileImageUrl(profileImageUrl)
                    .about(about)
                    .socialLinks(socialLinks)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "사용자 소셜 링크 정보 (공개용)")
    public static class SocialLinkItem {

        @Schema(description = "소셜 타입", example = "GITHUB")
        private SocialType socialType;

        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;

        public static SocialLinkItem of(SocialType socialType, String url) {
            return SocialLinkItem.builder()
                    .socialType(socialType)
                    .url(url)
                    .build();
        }
    }
}
