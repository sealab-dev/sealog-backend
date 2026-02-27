package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.enums.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserSocialLinkRequest {

    /**
     * 소셜 링크 전체 목록 upsert 요청
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 링크 전체 upsert 요청")
    public static class UpsertRequest {

        @Valid
        @NotNull(message = "소셜 링크 목록을 입력해주세요")
        @Schema(description = "소셜 링크 목록 (빈 배열 전송 시 전체 삭제)")
        private List<LinkItem> links;
    }

    /**
     * 소셜 링크 단건
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 링크 단건")
    public static class LinkItem {

        @NotNull(message = "소셜 타입을 입력해주세요")
        @Schema(description = "소셜 타입", example = "GITHUB")
        private SocialType socialType;

        @NotNull(message = "URL을 입력해주세요")
        @Size(max = 500, message = "URL은 500자 이내로 입력해주세요")
        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;
    }
}
