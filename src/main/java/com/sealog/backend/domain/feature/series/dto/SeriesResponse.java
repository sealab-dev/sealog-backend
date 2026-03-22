package com.sealog.backend.domain.feature.series.dto;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공개 시리즈 관련 응답 DTO
 */
public class SeriesResponse {

    /**
     * 공개 시리즈 목록 조회를 위한 정보 DTO
     */
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

    /**
     * 시리즈에 속한 게시글 요약 정보 DTO
     */
    @Getter
    @Builder
    @Schema(description = "시리즈 내 게시글 요약 정보")
    public static class SeriesPostItem {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 슬러그", example = "spring-boot-jpa-tips")
        private String slug;

        @Schema(description = "제목", example = "Spring Boot JPA 팁")
        private String title;

        @Schema(description = "본문 요약")
        private String excerpt;

        @Schema(description = "게시 상태", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL")
        private String thumbnailUrl;

        @Schema(description = "태그명 목록")
        private List<String> tags;

        @Schema(description = "카테고리 목록")
        private List<CategoryItem> categories;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        public static SeriesPostItem of(
                Long id, String slug, String title, String excerpt,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<CategoryItem> categories,
                AuthorInfo author, LocalDateTime createdAt
        ) {
            return SeriesPostItem.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).categories(categories).author(author).createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 작성자 요약 정보 (내부 전용)
     */
    @Getter
    @Builder
    public static class AuthorInfo {
        private String nickname;
        private String profileImageUrl;

        public static AuthorInfo of(String nickname, String profileImageUrl) {
            return AuthorInfo.builder().nickname(nickname).profileImageUrl(profileImageUrl).build();
        }
    }

    /**
     * 카테고리 요약 정보 (내부 전용)
     */
    @Getter
    @Builder
    public static class CategoryItem {
        private Long id;
        private String name;
        private Integer sortOrder;

        public static CategoryItem of(Long id, String name, Integer sortOrder) {
            return CategoryItem.builder().id(id).name(name).sortOrder(sortOrder).build();
        }
    }
}
