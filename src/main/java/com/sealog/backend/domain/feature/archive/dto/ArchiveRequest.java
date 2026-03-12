package com.sealog.backend.domain.feature.archive.dto;

import com.sealog.backend.domain.base.validation.annotation.CheckStringSize;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;
import lombok.experimental.UtilityClass;


/**
 * Archive Entity 관련 요청 DTO
 */
@UtilityClass
public class ArchiveRequest {

    /**
     * 아카이브 생성 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ArchiveCreate", description = "아카이브 생성 요청")
    public static class Create {

        @CheckStringSize(max = 100)
        @Schema(description = "이름", example = "Spring Boot 프로젝트", maxLength = 100)
        private String name;
    }

    /**
     * 아카이브 수정 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ArchiveUpdate", description = "아카이브 수정 요청")
    public static class Update {

        @CheckStringSize(max = 100)
        @Schema(description = "이름", example = "Spring Boot 프로젝트", maxLength = 100)
        private String name;
    }
}