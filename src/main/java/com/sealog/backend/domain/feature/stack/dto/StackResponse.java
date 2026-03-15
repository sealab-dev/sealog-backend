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

    @Getter
    @Builder
    @Schema(description = "스택 + 게시글 수 정보")
    public static class StackWithCount {
        private Long id;
        private String name;
        private StackGroup stackGroup;
        private Long postCount;

        public static StackWithCount of(Long id, String name, StackGroup stackGroup, Long postCount) {
            return StackWithCount.builder()
                    .id(id)
                    .name(name)
                    .stackGroup(stackGroup)
                    .postCount(postCount)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "그룹별 스택 목록")
    public static class GroupedStacks {
        private Map<StackGroup, List<StackWithCount>> groupedTags;

        public static GroupedStacks of(Map<StackGroup, List<StackWithCount>> groupedTags) {
            return GroupedStacks.builder()
                    .groupedTags(groupedTags)
                    .build();
        }
    }
}
