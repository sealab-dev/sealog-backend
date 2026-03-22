package com.sealog.backend.domain.feature.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 파일 관련 응답 DTO
 */
@Builder
@Schema(description = "파일 업로드 응답")
public record FileResponse(

        @Schema(description = "파일 메타데이터 ID", example = "42")
        Long id,

        @Schema(description = "원본 파일명", example = "profile.png")
        String originalName,

        @Schema(description = "저장 경로", example = "images/2024/01/uuid-profile.png")
        String path,

        @Schema(description = "파일 접근 URL", example = "https://cdn.example.com/images/2024/01/uuid-profile.png")
        String fileUrl,

        @Schema(description = "파일 크기 (bytes)", example = "204800")
        Long size,

        @Schema(description = "MIME 타입", example = "image/png")
        String contentType
) {
    /**
     * 파일 메타데이터 정보를 기반으로 응답 DTO를 생성합니다.
     */
    public static FileResponse of(Long id, String originalName, String path, String fileUrl, Long size, String contentType) {
        return FileResponse.builder()
                .id(id)
                .originalName(originalName)
                .path(path)
                .fileUrl(fileUrl)
                .size(size)
                .contentType(contentType)
                .build();
    }
}
