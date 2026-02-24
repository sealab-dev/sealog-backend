package com.sealog.backend.domain.feature.stack.dto;

import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.entity.StackGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class StackResponse {

    @Getter
    @Builder
    @Schema(description = "스택 기본 정보")
    public static class StackItem {

        @Schema(description = "스택 ID", example = "1")
        private Long id;

        @Schema(description = "스택명", example = "Spring Boot")
        private String name;

        @Schema(description = "스택 그룹", example = "ETC")
        private StackGroup stackGroup;

        public static StackItem from(Stack stack) {
            return StackItem.builder()
                    .id(stack.getId())
                    .name(stack.getName())
                    .stackGroup(stack.getStackGroup())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "스택 + 게시글 수")
    public static class StackWithCount {

        @Schema(description = "스택 ID", example = "1")
        private Long id;

        @Schema(description = "스택명", example = "Spring Boot")
        private String name;

        @Schema(description = "스택 그룹", example = "ETC")
        private StackGroup stackGroup;

        @Schema(description = "게시글 수", example = "12")
        private Long postCount;

        public static StackWithCount of(Stack stack, Long postCount) {
            return StackWithCount.builder()
                    .id(stack.getId())
                    .name(stack.getName())
                    .stackGroup(stack.getStackGroup())
                    .postCount(postCount)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "그룹별 스택 목록")
    public static class GroupedStacks {

        @Schema(description = "그룹명 → 스택 목록 맵")
        private Map<StackGroup, List<StackWithCount>> groupedTags;

        public static GroupedStacks of(Map<StackGroup, List<StackWithCount>> groupedTags) {
            return GroupedStacks.builder()
                    .groupedTags(groupedTags)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "인기 스택")
    public static class PopularStack {

        @Schema(description = "순위", example = "1")
        private int rank;

        @Schema(description = "스택 ID", example = "3")
        private Long id;

        @Schema(description = "스택명", example = "React")
        private String name;

        @Schema(description = "게시글 수", example = "25")
        private Long postCount;

        public static PopularStack of(int rank, Stack stack, Long postCount) {
            return PopularStack.builder()
                    .rank(rank)
                    .id(stack.getId())
                    .name(stack.getName())
                    .postCount(postCount)
                    .build();
        }
    }
}
