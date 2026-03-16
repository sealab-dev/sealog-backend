package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.enums.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class UserMeResponse {

    @Getter
    @Builder
    @Schema(description = "내 정보 응답")
    public static class MyProfile {
        private Long id;
        private String email;
        private String name;
        private String nickname;
        private String position;
        private String about;
        private String profileImageUrl;
        private List<MySocialLinkItem> socialLinks;

        public static MyProfile of(
                Long id,
                String email,
                String name,
                String nickname,
                String position,
                String about,
                String profileImageUrl,
                List<MySocialLinkItem> socialLinks
        ) {
            return MyProfile.builder()
                    .id(id)
                    .email(email)
                    .name(name)
                    .nickname(nickname)
                    .position(position)
                    .about(about)
                    .profileImageUrl(profileImageUrl)
                    .socialLinks(socialLinks)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "내 소셜 링크 정보")
    public static class MySocialLinkItem {

        @Schema(description = "소셜 타입", example = "GITHUB")
        private SocialType socialType;

        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;

        public static MySocialLinkItem of(SocialType socialType, String url) {
            return MySocialLinkItem.builder()
                    .socialType(socialType)
                    .url(url)
                    .build();
        }
    }
}
