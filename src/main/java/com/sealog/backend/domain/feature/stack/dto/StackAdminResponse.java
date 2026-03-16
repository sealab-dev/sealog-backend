package com.sealog.backend.domain.feature.stack.dto;

import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 어드민 스택 관련 응답 DTO
 */
public class StackAdminResponse {

    @Getter
    @Builder
    @Schema(description = "어드민용 스택 정보")
    public static class StackItem {
        private Long id;
        private String name;
        private StackGroup stackGroup;

        public static StackItem of(Long id, String name, StackGroup stackGroup) {
            return StackItem.builder()
                    .id(id)
                    .name(name)
                    .stackGroup(stackGroup)
                    .build();
        }
    }
}
