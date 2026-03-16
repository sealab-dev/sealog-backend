package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.enums.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class UserMeResponse {

    @Getter
    @Builder
    @Schema(description = "내 프로필 정보")
    public static class MyProfile {

        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "닉네임", example = "seadev")
        private String nickname;

        @Schema(description = "포지션 (없으면 null)", example = "Java/Spring 백엔드 개발자")
        private String position;

        @Schema(description = "소개 (없으면 null)", example = "Java/Spring 기반 백엔드 개발자입니다.")
        private String about;

        @Schema(description = "프로필 이미지 URL (없으면 null)", example = "https://cdn.example.com/profile/abc.jpg")
        private String profileImageUrl;

        @Schema(description = "소셜 링크 목록")
        private List<MySocialLinkItem> socialLinks;

        public static MyProfile of(
                Long id, String email, String name, String nickname,
                String position, String about,
                String profileImageUrl, List<MySocialLinkItem> socialLinks
        ) {
            return MyProfile.builder()
                    .id(id).email(email).name(name).nickname(nickname)
                    .position(position).about(about)
                    .profileImageUrl(profileImageUrl).socialLinks(socialLinks)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "내 소셜 링크 정보")
    public static class MySocialLinkItem {

        @Schema(description = "소셜 타입 (GITHUB / NOTION / PORTFOLIO / LINKEDIN / YOUTUBE / INSTAGRAM)", example = "GITHUB")
        private SocialType socialType;

        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;

        public static MySocialLinkItem of(SocialType socialType, String url) {
            return MySocialLinkItem.builder().socialType(socialType).url(url).build();
        }
    }
}
