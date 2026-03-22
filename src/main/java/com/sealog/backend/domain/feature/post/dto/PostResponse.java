package com.sealog.backend.domain.feature.post.dto;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 관련 응답 DTO (공개용)
 */
public class PostResponse {

    /**
     * 게시글 목록 조회를 위한 요약 정보 DTO
     */
    @Getter
    @Builder
    @Schema(description = "게시글 목록 요약 정보")
    public static class PostItem {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 URL 슬러그", example = "spring-boot-jpa-tips")
        private String slug;

        @Schema(description = "제목", example = "Spring Boot JPA 팁")
        private String title;

        @Schema(description = "본문 앞부분 요약", example = "JPA를 사용할 때 알아두면 좋은 팁들을 정리했습니다.")
        private String excerpt;

        @Schema(description = "게시 상태", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "https://cdn.example.com/thumbnail/abc.jpg")
        private String thumbnailUrl;

        @Schema(description = "태그 목록")
        private List<String> tags;

        @Schema(description = "카테고리 목록")
        private List<CategoryItem> categories;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        public static PostItem of(
                Long id, String slug, String title, String excerpt,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<CategoryItem> categories,
                AuthorInfo author, LocalDateTime createdAt
        ) {
            return PostItem.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).categories(categories).author(author).createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 게시글 상세 조회를 위한 정보 DTO
     */
    @Getter
    @Builder
    @Schema(description = "게시글 상세 정보")
    public static class PostDetail {

        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 URL 슬러그", example = "spring-boot-jpa-tips")
        private String slug;

        @Schema(description = "제목", example = "Spring Boot JPA 팁")
        private String title;

        @Schema(description = "본문 요약", example = "JPA를 사용할 때 알아두면 좋은 팁들을 정리했습니다.")
        private String excerpt;

        @Schema(description = "본문 전체 (HTML)")
        private String content;

        @Schema(description = "게시 상태", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL (없으면 null)")
        private String thumbnailUrl;

        @Schema(description = "태그 목록")
        private List<String> tags;

        @Schema(description = "카테고리 목록")
        private List<CategoryItem> categories;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "소속 시리즈 정보 (없으면 null)")
        private SeriesInfo seriesInfo;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "마지막 수정일시")
        private LocalDateTime updatedAt;

        public static PostDetail of(
                Long id, String slug, String title, String excerpt, String content,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<CategoryItem> categories,
                AuthorInfo author, SeriesInfo seriesInfo,
                LocalDateTime createdAt, LocalDateTime updatedAt
        ) {
            return PostDetail.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt).content(content)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).categories(categories).author(author).seriesInfo(seriesInfo)
                    .createdAt(createdAt).updatedAt(updatedAt)
                    .build();
        }
    }

    /**
     * 작성자 요약 정보
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
     * 게시글에 연결된 카테고리 요약 정보
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

    /**
     * 게시글이 속한 시리즈 요약 정보
     */
    @Getter
    @Builder
    public static class SeriesInfo {
        private Long id;
        private String slug;
        private String name;

        public static SeriesInfo of(Long id, String slug, String name) {
            return SeriesInfo.builder().id(id).slug(slug).name(name).build();
        }
    }
}
