package com.sealog.backend.feature.stack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StackRequest {

    /* 태그 생성 요청*/
    @Getter
    @Builder
    @NoArgsConstructor@AllArgsConstructor
    @Schema(description = "스택 생성/수정 요청")
    public static class Create {

        @NotBlank(message = "스택명을 입력해주세요")
        @Size(max = 50, message = "스택명은 20자 이내로 입력해주세요")
        @Schema(description = "스택명", example = "Spring Boot", maxLength = 50)
        private String name;

        @NotBlank(message = "태그그룹이 선택되지 않았습니다")
        @Schema(description = "스택 그룹", example = "BACKEND")
        private String stackGroup;
    }

    /* 태그 수정 요청*/
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "스택 생성/수정 요청")
    public static class Update {

        @NotBlank(message = "스택명을 입력해주세요")
        @Size(max = 50, message = "스택명은 20자 이내로 입력해주세요")
        private String name;

        @NotBlank(message = "태그그룹이 선택되지 않았습니다")
        private String stackGroup;
    }
}