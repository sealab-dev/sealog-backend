package com.sealog.backend.domain.feature.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryAdminRequest {

    @Getter
    @NoArgsConstructor
    public static class Create {

        @NotBlank(message = "카테고리명은 필수입니다.")
        @Size(max = 50, message = "카테고리명은 최대 50자입니다.")
        @Schema(description = "카테고리명", example = "Spring Boot")
        private String name;

        @NotBlank(message = "카테고리 그룹은 필수입니다.")
        @Schema(description = "카테고리 그룹 키", example = "FRAMEWORK")
        private String categoryGroup;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {

        @NotBlank(message = "카테고리명은 필수입니다.")
        @Size(max = 50, message = "카테고리명은 최대 50자입니다.")
        @Schema(description = "수정할 카테고리명", example = "Spring Boot 3.0")
        private String name;

        @Schema(description = "수정할 카테고리 그룹 키", example = "LIBRARY")
        private String categoryGroup;
    }
}
