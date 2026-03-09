package com.sealog.backend.domain.feature.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PostRequest {

    /**
     * 게시글 생성 요청
     */

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PostCreate", description = "게시글 생성 요청")
    public static class Create {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요")
        @Schema(description = "제목", example = "Spring Boot 시작하기", maxLength = 50)
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 200, message = "요약은 200자 이내로 입력해주세요")
        @Schema(description = "요약", example = "Spring Boot 핵심 개념을 정리합니다.", maxLength = 200)
        private String excerpt;

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

        @Size(max = 5, message = "스택은 최대 5개까지 등록할 수 있습니다")
        @Schema(description = "스택 ID 목록", example = "[1, 3, 5]")
        private List<Long> stackIds;

    }

    /**
     * 게시글 수정 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PostUpdate", description = "게시글 수정 요청")
    public static class Update {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요")
        @Schema(description = "제목", example = "Spring Boot 시작하기", maxLength = 50)
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 200, message = "요약은 200자 이내로 입력해주세요")
        @Schema(description = "요약", example = "Spring Boot 핵심 개념을 정리합니다.", maxLength = 200)
        private String excerpt;

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

        @Size(max = 5, message = "스택은 최대 5개까지 등록할 수 있습니다")
        @Schema(description = "스택 ID 목록", example = "[1, 3, 5]")
        private List<Long> stackIds;

    }
}