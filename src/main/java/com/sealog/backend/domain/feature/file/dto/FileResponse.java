package com.sealog.backend.domain.feature.file.dto;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import lombok.Builder;

@Builder
public record FileResponse(
        Long id,
        String originalName,
        String path,
        String fileUrl,
        Long size,
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