package com.sealog.backend.domain.feature.series.dto;

import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공개 시리즈 관련 응답 DTO
 */
public class SeriesResponse {

    @Getter
    @Builder
    @Schema(description = "공개 시리즈 목록 정보")
    public static class SeriesItem {

        @Schema(description = "시리즈 ID", example = "3")
        private Long id;

        @Schema(description = "시리즈 슬러그", example = "spring-series")
        private String slug;

        @Schema(description = "시리즈명", example = "Spring Boot 시리즈")
        private String name;

        @Schema(description = "시리즈 내 공개 게시글 수", example = "5")
        private Long postCount;

        public static SeriesItem of(Long id, String slug, String name, Long postCount) {
            return SeriesItem.builder().id(id).slug(slug).name(name).postCount(postCount).build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "시리즈에 속한 공개 게시글 목록 정보")
    public static class SeriesPostItem {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 슬러그", example = "spring-boot-jpa-tips")
        private String slug;

        @Schema(description = "제목", example = "Spring Boot JPA 팁")
        private String title;

        @Schema(description = "본문 앞부분 요약", example = "JPA를 사용할 때 알아두면 좋은 팁들을 정리했습니다.")
        private String excerpt;

        @Schema(description = "게시 상태 (PUBLISHED: 공개)", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "https://cdn.example.com/thumbnail/abc.jpg")
        private String thumbnailUrl;

        @Schema(description = "태그 목록 (최대 3개)", example = "[\"spring\", \"jpa\"]")
        private List<String> tags;

        @Schema(description = "기술 스택 목록 (최대 5개)")
        private List<PostResponse.StackItem> stacks;

        @Schema(description = "작성자 정보")
        private PostResponse.AuthorInfo author;

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        public static SeriesPostItem of(
                Long id, String slug, String title, String excerpt,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<PostResponse.StackItem> stacks,
                PostResponse.AuthorInfo author, LocalDateTime createdAt
        ) {
            return SeriesPostItem.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).stacks(stacks).author(author).createdAt(createdAt)
                    .build();
        }
    }
}
