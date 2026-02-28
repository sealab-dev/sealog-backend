package com.sealog.backend.domain.feature.achieve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.UtilityClass;


/**
 * Archive Entity 관련 응답 DTO
 */
@UtilityClass
public class ArchiveResponse {

    /**
     * 아카이브 목록 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "아카이브 목록 조회")
    public static class ArchiveItems {

        private Long id;
        private String slug;
        private String name;

        public static ArchiveItems of(Long id, String slug, String name) {

            return ArchiveItems.builder()
                    .id(id)
                    .slug(slug)
                    .name(name)
                    .build();
        }
    }

}