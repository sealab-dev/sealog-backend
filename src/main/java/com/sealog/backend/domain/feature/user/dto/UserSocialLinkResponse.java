package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.entity.UserSocial;
import com.sealog.backend.domain.feature.user.enums.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class UserSocialLinkResponse {

    /**
     * 소셜 링크 단건 응답
     */
    @Getter
    @Builder
    @Schema(description = "소셜 링크 정보")
    public static class LinkInfo {

        @Schema(description = "소셜 링크 ID", example = "1")
        private Long id;

        @Schema(description = "소셜 타입", example = "GITHUB")
        private SocialType socialType;

        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;

        public static LinkInfo from(UserSocial link) {
            return LinkInfo.builder()
                    .id(link.getId())
                    .socialType(link.getSocialType())
                    .url(link.getUrl())
                    .build();
        }
    }
}
