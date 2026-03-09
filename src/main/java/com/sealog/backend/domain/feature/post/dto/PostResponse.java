package com.sealog.backend.domain.feature.post.dto;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    /**
     * 작성자 정보
     */
    @Getter
    @Builder
    @Schema(name = "PostUserInfo")
    public static class AuthorInfo {
        private String nickname;
        private String profileImagePath;

        public static AuthorInfo of(String nickname, String profileImagePath) {
            return AuthorInfo.builder()
                    .nickname(nickname)
                    .profileImagePath(profileImagePath)
                    .build();
        }
    }

    /**
     * 스택 정보 (post 도메인 내부 관리)
     * - stack 도메인 DTO 의존 제거
     * - sortOrder 포함
     */
    @Getter
    @Builder
    @Schema(name = "PostStackItem")
    public static class StackItem {
        private Long id;
        private String name;
        private Integer sortOrder;

        public static StackItem of(Long id, String name, Integer sortOrder) {
            return StackItem.builder()
                    .id(id)
                    .name(name)
                    .sortOrder(sortOrder)
                    .build();
        }
    }

    /**
     * 게시글 목록 응답 (요약 정보)
     */
    @Getter
    @Builder
    public static class PostItems {

        private Long id;
        private String slug;
        private String title;
        private String excerpt;
        private PostStatus status;
        private String thumbnailPath;
        private List<String> tags;
        private List<StackItem> stacks;
        private AuthorInfo author;
        private LocalDateTime createdAt;

        public static PostItems of(
                Long id,
                String slug,
                String title,
                String excerpt,
                PostStatus status,
                String thumbnailPath,
                List<String> tags,
                List<StackItem> stacks,
                AuthorInfo author,
                LocalDateTime createdAt
        ) {
            return PostItems.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .status(status)
                    .thumbnailPath(thumbnailPath)
                    .tags(tags)
                    .stacks(stacks)
                    .author(author)
                    .createdAt(createdAt)
                    .build();
        }
    }

    /**
     * 게시글 상세 응답 (전체 정보 + 연관 게시글)
     */
    @Getter
    @Builder
    @Schema(name = "PostDetail")
    public static class Detail {

        private Long id;
        private String slug;
        private String title;
        private String excerpt;
        private String content;
        private PostStatus status;
        private String thumbnailPath;
        private List<String> tags;
        private List<StackItem> stacks;
        private AuthorInfo author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Detail of(
                Long id,
                String slug,
                String title,
                String excerpt,
                String content,
                PostStatus status,
                String thumbnailPath,
                List<String> tags,
                List<StackItem> stacks,
                AuthorInfo author,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
            return Detail.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .content(content)
                    .status(status)
                    .thumbnailPath(thumbnailPath)
                    .tags(tags)
                    .stacks(stacks)
                    .author(author)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
    }

    /**
     * 게시글 수정용 응답
     */
    @Getter
    @Builder
    @Schema(name = "PostEdit")
    public static class Edit {

        private Long id;
        private String slug;
        private String title;
        private String excerpt;
        private String content;
        private PostStatus status;
        private String thumbnailPath;
        private List<String> tags;
        private List<StackItem> stacks;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Edit of(
                Long id,
                String slug,
                String title,
                String excerpt,
                String content,
                PostStatus status,
                String thumbnailPath,
                List<String> tags,
                List<StackItem> stacks,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
            return Edit.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .content(content)
                    .status(status)
                    .thumbnailPath(thumbnailPath)
                    .tags(tags)
                    .stacks(stacks)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
    }
}