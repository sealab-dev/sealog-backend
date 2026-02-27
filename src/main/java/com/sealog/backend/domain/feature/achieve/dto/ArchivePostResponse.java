package com.sealog.backend.domain.feature.achieve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.UtilityClass;


/**
 * ArchiveItem Entity 관련 응답 DTO
 */
@UtilityClass
public class ArchivePostResponse {

    /**
     * 아카이브 목록 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "블로그 아카이브 목록 조회")
    public static class ArchiveItemItems {

        private Long id;
        private String slug;
        private String name;

        public static ArchiveItemItems of(Long id, String slug, String name) {

            return ArchiveItemItems.builder()
                    .id(id)
                    .slug(slug)
                    .name(name)
                    .build();
        }
    }

}