package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.entity.UserSocial;
import com.sealog.backend.domain.feature.user.enums.SocialType;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
        private List<SocialLinkItem> socialLinks;

        public static MyProfile of(User user, String profileImageUrl, List<SocialLinkItem> socialLinks) {
            return MyProfile.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .position(user.getPosition())
                    .about(user.getAbout())
                    .profileImageUrl(profileImageUrl)
                    .socialLinks(socialLinks)
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
        private List<SocialLinkItem> socialLinks;

        public static PublicProfile of(User user, String profileImageUrl, List<SocialLinkItem> socialLinks) {
            return PublicProfile.builder()
                    .nickname(user.getNickname())
                    .profileImageUrl(profileImageUrl)
                    .position(user.getPosition())
                    .about(user.getAbout())
                    .socialLinks(socialLinks)
                    .build();
        }
    }

    /**
     * 소셜 링크 응답
     */
    @Getter
    @Builder
    @Schema(description = "소셜 링크 정보")
    public static class SocialLinkItem {

        @Schema(description = "소셜 타입", example = "GITHUB")
        private SocialType socialType;

        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;

        public static SocialLinkItem from(UserSocial link) {
            return SocialLinkItem.builder()
                    .socialType(link.getSocialType())
                    .url(link.getUrl())
                    .build();
        }
    }
}
