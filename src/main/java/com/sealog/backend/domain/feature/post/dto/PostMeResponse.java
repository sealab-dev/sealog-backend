package com.sealog.backend.domain.feature.post.dto;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class PostMeResponse {

    /**
     * 내 게시글 목록 응답
     */
    @Getter
    @Builder
    @Schema(description = "내 게시글 목록 정보")
    public static class MyPostItem {

        private Long id;
        private String slug;
        private String title;
        private String excerpt;
        private PostStatus status;
        private String thumbnailUrl;
        private List<String> tags;
        private List<MyStackItem> stacks;
        private LocalDateTime createdAt;

        public static MyPostItem of(
                Long id,
                String slug,
                String title,
                String excerpt,
                PostStatus status,
                String thumbnailUrl,
                List<String> tags,
                List<MyStackItem> stacks,
                LocalDateTime createdAt
        ) {
            return MyPostItem.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .status(status)
                    .thumbnailUrl(thumbnailUrl)
                    .tags(tags)
                    .stacks(stacks)
                    .createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 게시글 수정용 응답
     */
    @Getter
    @Builder
    @Schema(description = "게시글 수정용 상세 정보")
    public static class MyPostEdit {

        private Long id;
        private String slug;
        private String title;
        private String excerpt;
        private String content;
        private PostStatus status;
        private String thumbnailUrl;
        private List<String> tags;
        private List<MyStackItem> stacks;
        private Long seriesId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static MyPostEdit of(
                Long id,
                String slug,
                String title,
                String excerpt,
                String content,
                PostStatus status,
                String thumbnailUrl,
                List<String> tags,
                List<MyStackItem> stacks,
                Long seriesId,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
            return MyPostEdit.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .content(content)
                    .status(status)
                    .thumbnailUrl(thumbnailUrl)
                    .tags(tags)
                    .stacks(stacks)
                    .seriesId(seriesId)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
    }

    /**
     * 스택 정보
     */
    @Getter
    @Builder
    @Schema(description = "게시글 관련 스택 정보")
    public static class MyStackItem {
        private Long id;
        private String name;
        private Integer sortOrder;

        public static MyStackItem of(Long id, String name, Integer sortOrder) {
            return MyStackItem.builder()
                    .id(id)
                    .name(name)
                    .sortOrder(sortOrder)
                    .build();
        }
    }
}
