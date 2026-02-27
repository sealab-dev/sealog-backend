package com.sealog.backend.domain.feature.achieve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;


/**
 * ArchiveItem Entity 관련 요청 DTO
 */
@UtilityClass
public class ArchivePostRequest {

    /**
     * 게시글 생성 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "아카이브 생성 요청")
    public static class Add {

        @NotEmpty(message = "아카이브번호는 반드시 존재해야 합니다.")
        @Schema(description = "아카이브 번호 목록", example = "1L")
        private List<Long> archiveIds = new ArrayList<>();
    }

}