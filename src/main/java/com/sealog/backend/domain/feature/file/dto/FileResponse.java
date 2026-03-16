package com.sealog.backend.domain.feature.file.dto;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "파일 업로드 응답")
public record FileResponse(

        @Schema(description = "파일 메타데이터 ID", example = "42")
        Long id,

        @Schema(description = "원본 파일명", example = "profile.png")
        String originalName,

        @Schema(description = "S3 저장 경로", example = "images/2024/01/uuid-profile.png")
        String path,

        @Schema(description = "파일 접근 URL", example = "https://cdn.example.com/images/2024/01/uuid-profile.png")
        String fileUrl,

        @Schema(description = "파일 크기 (bytes)", example = "204800")
        Long size,

        @Schema(description = "MIME 타입", example = "image/png")
        String contentType
) {
    public static FileResponse from(FileMetadata fileMetadata, String fileUrl) {
        return FileResponse.builder()
                .id(fileMetadata.getId())
                .originalName(fileMetadata.getOriginalName())
                .path(fileMetadata.getPath())
                .fileUrl(fileUrl)
                .size(fileMetadata.getSize())
                .contentType(fileMetadata.getContentType())
                .build();
    }
}
