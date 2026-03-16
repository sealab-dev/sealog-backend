package com.sealog.backend.domain.feature.post.dto;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    /**
     * 게시글 목록 응답 (요약 정보)
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

        @Schema(description = "게시 상태 (PUBLISHED: 공개 / PRIVATE: 비공개 / DRAFT: 작성 중)", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "https://cdn.example.com/thumbnail/abc.jpg")
        private String thumbnailUrl;

        @Schema(description = "태그 목록 (최대 3개)", example = "[\"spring\", \"jpa\"]")
        private List<String> tags;

        @Schema(description = "기술 스택 목록 (최대 5개)")
        private List<StackItem> stacks;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        public static PostItem of(
                Long id, String slug, String title, String excerpt,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<StackItem> stacks,
                AuthorInfo author, LocalDateTime createdAt
        ) {
            return PostItem.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).stacks(stacks).author(author).createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 게시글 상세 응답
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

        @Schema(description = "본문 앞부분 요약", example = "JPA를 사용할 때 알아두면 좋은 팁들을 정리했습니다.")
        private String excerpt;

        @Schema(description = "본문 전체 (HTML)", example = "<h2>소제목</h2><p>본문 내용...</p>")
        private String content;

        @Schema(description = "게시 상태 (PUBLISHED: 공개 / PRIVATE: 비공개 / DRAFT: 작성 중)", example = "PUBLISHED")
        private PostStatus status;

        @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "https://cdn.example.com/thumbnail/abc.jpg")
        private String thumbnailUrl;

        @Schema(description = "태그 목록 (최대 3개)", example = "[\"spring\", \"jpa\"]")
        private List<String> tags;

        @Schema(description = "기술 스택 목록 (최대 5개)")
        private List<StackItem> stacks;

        @Schema(description = "작성자 정보")
        private AuthorInfo author;

        @Schema(description = "소속 시리즈 정보 (없으면 null)")
        private SeriesInfo seriesInfo;

        @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "마지막 수정일시", example = "2024-01-20T14:00:00")
        private LocalDateTime updatedAt;

        public static PostDetail of(
                Long id, String slug, String title, String excerpt, String content,
                PostStatus status, String thumbnailUrl,
                List<String> tags, List<StackItem> stacks,
                AuthorInfo author, SeriesInfo seriesInfo,
                LocalDateTime createdAt, LocalDateTime updatedAt
        ) {
            return PostDetail.builder()
                    .id(id).slug(slug).title(title).excerpt(excerpt).content(content)
                    .status(status).thumbnailUrl(thumbnailUrl)
                    .tags(tags).stacks(stacks).author(author).seriesInfo(seriesInfo)
                    .createdAt(createdAt).updatedAt(updatedAt)
                    .build();
        }
    }

    /**
     * 작성자 정보
     */
    @Getter
    @Builder
    @Schema(description = "작성자 정보")
    public static class AuthorInfo {

        @Schema(description = "작성자 닉네임", example = "seadev")
        private String nickname;

        @Schema(description = "작성자 프로필 이미지 URL (없으면 null)", example = "https://cdn.example.com/profile/abc.jpg")
        private String profileImageUrl;

        public static AuthorInfo of(String nickname, String profileImageUrl) {
            return AuthorInfo.builder().nickname(nickname).profileImageUrl(profileImageUrl).build();
        }
    }

    /**
     * 스택 정보 (post 도메인 내부 관리)
     */
    @Getter
    @Builder
    @Schema(description = "게시글에 연결된 기술 스택")
    public static class StackItem {

        @Schema(description = "스택 ID", example = "1")
        private Long id;

        @Schema(description = "스택명", example = "Spring Boot")
        private String name;

        @Schema(description = "정렬 순서", example = "1")
        private Integer sortOrder;

        public static StackItem of(Long id, String name, Integer sortOrder) {
            return StackItem.builder().id(id).name(name).sortOrder(sortOrder).build();
        }
    }

    /**
     * 시리즈 정보
     */
    @Getter
    @Builder
    @Schema(description = "게시글이 속한 시리즈 요약 정보")
    public static class SeriesInfo {

        @Schema(description = "시리즈 ID", example = "3")
        private Long id;

        @Schema(description = "시리즈 슬러그", example = "spring-series")
        private String slug;

        @Schema(description = "시리즈명", example = "Spring Boot 시리즈")
        private String name;

        public static SeriesInfo of(Long id, String slug, String name) {
            return SeriesInfo.builder().id(id).slug(slug).name(name).build();
        }
    }
}
