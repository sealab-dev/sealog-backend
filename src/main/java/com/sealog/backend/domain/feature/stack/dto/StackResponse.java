package com.sealog.backend.domain.feature.stack.dto;

import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 공개 스택 관련 응답 DTO
 */
public class StackResponse {

    @Getter
    @Builder
    @Schema(description = "스택 기본 정보")
    public static class StackItem {

        @Schema(description = "스택 ID", example = "1")
        private Long id;

        @Schema(description = "스택명", example = "Spring Boot")
        private String name;

        @Schema(description = "스택 그룹 (LANGUAGE / FRAMEWORK / LIBRARY / DATABASE / DEVOPS / KNOWLEDGE / TOOL / ETC)",
                example = "FRAMEWORK")
        private StackGroup stackGroup;

        public static StackItem of(Long id, String name, StackGroup stackGroup) {
            return StackItem.builder().id(id).name(name).stackGroup(stackGroup).build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "스택 + 해당 스택으로 작성된 게시글 수")
    public static class StackWithCount {

        @Schema(description = "스택 ID", example = "1")
        private Long id;

        @Schema(description = "스택명", example = "Spring Boot")
        private String name;

        @Schema(description = "스택 그룹 (LANGUAGE / FRAMEWORK / LIBRARY / DATABASE / DEVOPS / KNOWLEDGE / TOOL / ETC)",
                example = "FRAMEWORK")
        private StackGroup stackGroup;

        @Schema(description = "해당 스택으로 작성된 공개 게시글 수", example = "12")
        private Long postCount;

        public static StackWithCount of(Long id, String name, StackGroup stackGroup, Long postCount) {
            return StackWithCount.builder().id(id).name(name).stackGroup(stackGroup).postCount(postCount).build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "스택 그룹별 목록 (groupedTags 키: LANGUAGE / FRAMEWORK 등)")
    public static class GroupedStacks {

        @Schema(description = "그룹(StackGroup)을 키로 하는 스택 목록 맵")
        private Map<StackGroup, List<StackWithCount>> groupedTags;

        public static GroupedStacks of(Map<StackGroup, List<StackWithCount>> groupedTags) {
            return GroupedStacks.builder().groupedTags(groupedTags).build();
        }
    }
}
