package com.sealog.backend.domain.feature.post.dto;

import com.sealog.backend.domain.feature.post.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

public class PostRequest {

    /**
     * 게시글 생성 요청
     */

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "게시글 생성 요청")
    public static class Create {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요")
        @Schema(description = "제목", example = "Spring Boot 시작하기", maxLength = 50)
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 200, message = "요약은 200자 이내로 입력해주세요")
        @Schema(description = "요약", example = "Spring Boot 핵심 개념을 정리합니다.", maxLength = 200)
        private String excerpt;

        @NotNull(message = "타입을 선택해주세요")
        @Schema(description = "게시글 타입", example = "CORE")
        private PostType postType;

        @NotBlank(message = "내용을 입력해주세요")
        @Size(max = 50000, message = "본문은 50000자 이내로 입력해주세요")
        @Schema(description = "본문", example = "내용 여기에 본문을 작성합니다.", maxLength = 50000)
        private String content;

        @Schema(description = "썸네일 파일 ID", example = "10")
        private Long thumbnailFileId;

        @Schema(description = "썸네일 경로", example = "")
        private String thumbnailPath;

        @Schema(description = "태그 목록", example = "[\"spring\", \"jwt\"]")
        private List<String> tags;

        @Schema(description = "스택 목록", example = "[\"Spring Boot\", \"JPA\"]")
        private Set<String> stacks;

    }

    /**
     * 게시글 수정 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "게시글 수정 요청")
    public static class Update {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요")
        @Schema(description = "제목", example = "Spring Boot 시작하기", maxLength = 50)
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 200, message = "요약은 200자 이내로 입력해주세요")
        @Schema(description = "요약", example = "Spring Boot 핵심 개념을 정리합니다.", maxLength = 200)
        private String excerpt;

        @NotNull(message = "타입을 선택해주세요")
        @Schema(description = "게시글 타입", example = "CORE")
        private PostType postType;

        @NotBlank(message = "내용을 입력해주세요")
        @Size(max = 50000, message = "본문은 50000자 이내로 입력해주세요")
        @Schema(description = "본문", example = "내용 여기에 본문을 작성합니다.", maxLength = 50000)
        private String content;

        @Schema(description = "썸네일 파일 ID", example = "10")
        private Long thumbnailFileId;

        @Schema(description = "썸네일 경로", example = "https://cdn.example.com/thumb/10.png")
        private String thumbnailPath;

        @Schema(description = "썸네일 제거 여부", example = "false")
        private Boolean removeThumbnail;

        @Schema(description = "태그 목록", example = "[\"spring\", \"jwt\"]")
        private List<String> tags;

        @Schema(description = "스택 목록", example = "[\"Spring Boot\", \"JPA\"]")
        private Set<String> stacks;

    }
}