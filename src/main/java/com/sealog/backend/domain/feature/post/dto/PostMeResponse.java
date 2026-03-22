package com.sealog.backend.domain.feature.post.dto;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.category.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인증된 사용자의 게시글 관련 응답 DTO
 */
public class PostMeResponse {

    /**
     * 내 게시글 목록 조회를 위한 정보 DTO (관리 데이터 포함)
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

        @Schema(description = "본문 요약")
        private String excerpt;

        @Schema(description = "게시 상태 (PUBLISHED / PRIVATE / DRAFT)", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL")
        private String thumbnailUrl;

        @Schema(description = "태그 목록")
        private List<CategoryResponse.CategoryItem> tags;

        @Schema(description = "카테고리 목록")
        private List<CategoryResponse.CategoryItem> categories;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        public static MyPostItem of(
                Long id, String slug, String title, String excerpt,
                PostStatus status, String thumbnailUrl,
                List<CategoryResponse.CategoryItem> categories,
                List<CategoryResponse.CategoryItem> tags,
                LocalDateTime createdAt
        ) {
            return MyPostItem.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .status(status)
                    .thumbnailUrl(thumbnailUrl)
                    .categories(categories)
                    .tags(tags)
                    .createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 게시글 수정을 위한 기존 데이터 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "게시글 수정용 상세 정보")
    public static class MyPostEdit {

        @Schema(description = "게시글 ID")
        private Long id;

        @Schema(description = "게시글 슬러그")
        private String slug;

        @Schema(description = "제목")
        private String title;

        @Schema(description = "본문 요약")
        private String excerpt;

        @Schema(description = "본문 전체 (HTML/Markdown)")
        private String content;

        @Schema(description = "게시 상태")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL")
        private String thumbnailUrl;

        @Schema(description = "소속 시리즈 ID")
        private Long seriesId;

        @Schema(description = "카테고리 목록")
        private List<MyCategoryItem> categories;

        @Schema(description = "태그명 목록")
        private List<String> tagNames;

        public static MyPostEdit of(
                Long id, String slug, String title, String excerpt, String content,
                PostStatus status, String thumbnailUrl, Long seriesId,
                List<MyCategoryItem> categories, List<String> tagNames
        ) {
            return MyPostEdit.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt).content(content)
                    .status(status).thumbnailUrl(thumbnailUrl).seriesId(seriesId)
                    .categories(categories).tagNames(tagNames)
                    .build();
        }
    }

    /**
     * 수정을 위한 카테고리 매핑 정보 DTO
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
