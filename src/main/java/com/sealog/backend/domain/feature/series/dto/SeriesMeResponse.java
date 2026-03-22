package com.sealog.backend.domain.feature.series.dto;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 시리즈 관련 응답 DTO
 */
public class SeriesMeResponse {

    /**
     * 내 시리즈 목록 조회를 위한 정보 DTO
     */
    @Getter
    @Builder
    @Schema(description = "내 시리즈 목록 정보")
    public static class MySeriesItem {

        @Schema(description = "시리즈 ID", example = "3")
        private Long id;

        @Schema(description = "시리즈 슬러그", example = "spring-series")
        private String slug;

        @Schema(description = "시리즈명", example = "Spring Boot 시리즈")
        private String name;

        @Schema(description = "공개 여부 (true: 공개 / false: 비공개)", example = "true")
        private Boolean isPublic;

        @Schema(description = "시리즈 내 게시글 수", example = "5")
        private Long postCount;

        public static MySeriesItem of(Long id, String slug, String name, Boolean isPublic, Long postCount) {
            return MySeriesItem.builder().id(id).slug(slug).name(name).isPublic(isPublic).postCount(postCount).build();
        }
    }

    /**
     * 내 시리즈에 속한 게시글 요약 정보 DTO
     */
    @Getter
    @Builder
    @Schema(description = "내 시리즈 내 게시글 요약 정보")
    public static class MySeriesPostItem {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 슬러그", example = "spring-boot-jpa-tips")
        private String slug;

        @Schema(description = "제목", example = "Spring Boot JPA 팁")
        private String title;

        @Schema(description = "본문 요약")
        private String excerpt;

        @Schema(description = "게시 상태 (PUBLISHED / PRIVATE / DRAFT)", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL")
        private String thumbnailUrl;

        @Schema(description = "태그명 목록")
        private List<String> tags;

        @Schema(description = "카테고리 목록")
        private List<MyCategoryItem> categories;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        public static MySeriesPostItem of(
                Long id, String slug, String title, String excerpt,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<MyCategoryItem> categories,
                LocalDateTime createdAt
        ) {
            return MySeriesPostItem.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).categories(categories).createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 카테고리 요약 정보 (내부 전용)
     */
    @Getter
    @Builder
    public static class MyCategoryItem {
        private Long id;
        private String name;
        private Integer sortOrder;

        public static MyCategoryItem of(Long id, String name, Integer sortOrder) {
            return MyCategoryItem.builder().id(id).name(name).sortOrder(sortOrder).build();
        }
    }
}
