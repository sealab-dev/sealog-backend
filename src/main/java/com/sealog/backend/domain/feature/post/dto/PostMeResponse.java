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

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 URL 슬러그", example = "spring-boot-jpa-tips")
        private String slug;

        @Schema(description = "제목", example = "Spring Boot JPA 팁")
        private String title;

        @Schema(description = "본문 앞부분 요약", example = "JPA를 사용할 때 알아두면 좋은 팁들을 정리했습니다.")
        private String excerpt;

        @Schema(description = "게시 상태 (PUBLISHED: 공개 / PRIVATE: 비공개 / DRAFT: 작성 중)", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "https://cdn.example.com/thumbnail/abc.jpg")
        private String thumbnailUrl;

        @Schema(description = "태그 목록 (최대 3개)", example = "[\"spring\", \"jpa\"]")
        private List<String> tags;

        @Schema(description = "기술 스택 목록 (최대 5개)")
        private List<MyStackItem> stacks;

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        public static MyPostItem of(
                Long id, String slug, String title, String excerpt,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<MyStackItem> stacks, LocalDateTime createdAt
        ) {
            return MyPostItem.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).stacks(stacks).createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 게시글 수정용 응답
     */
    @Getter
    @Builder
    @Schema(description = "게시글 수정 페이지용 상세 정보")
    public static class MyPostEdit {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 URL 슬러그", example = "spring-boot-jpa-tips")
        private String slug;

        @Schema(description = "제목", example = "Spring Boot JPA 팁")
        private String title;

        @Schema(description = "본문 (HTML 또는 Markdown)", example = "<h2>소제목</h2><p>본문 내용...</p>")
        private String content;

        @Schema(description = "게시 상태 (PUBLISHED / PRIVATE / DRAFT)", example = "DRAFT")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "https://cdn.example.com/thumbnail/abc.jpg")
        private String thumbnailUrl;

        @Schema(description = "태그 목록", example = "[\"spring\", \"jpa\"]")
        private List<String> tags;

        @Schema(description = "기술 스택 목록")
        private List<MyStackItem> stacks;

        @Schema(description = "소속 시리즈 ID (없으면 null)", example = "3")
        private Long seriesId;

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "마지막 수정일시", example = "2024-01-20T14:00:00")
        private LocalDateTime updatedAt;

        public static MyPostEdit of(
                Long id, String slug, String title, String content,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<MyStackItem> stacks,
                Long seriesId, LocalDateTime createdAt, LocalDateTime updatedAt
        ) {
            return MyPostEdit.builder()
                    .id(id).slug(slug).title(title).content(content)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).stacks(stacks).seriesId(seriesId)
                    .createdAt(createdAt).updatedAt(updatedAt)
                    .build();
        }
    }

    /**
     * 스택 정보
     */
    @Getter
    @Builder
    @Schema(description = "게시글에 연결된 기술 스택")
    public static class MyStackItem {

        @Schema(description = "스택 ID", example = "1")
        private Long id;

        @Schema(description = "스택명", example = "Spring Boot")
        private String name;

        @Schema(description = "정렬 순서", example = "1")
        private Integer sortOrder;

        public static MyStackItem of(Long id, String name, Integer sortOrder) {
            return MyStackItem.builder().id(id).name(name).sortOrder(sortOrder).build();
        }
    }
}
