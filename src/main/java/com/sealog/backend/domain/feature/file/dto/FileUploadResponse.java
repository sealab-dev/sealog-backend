package com.sealog.backend.domain.feature.file.dto;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import lombok.Builder;

@Builder
public record FileUploadResponse(
        Long id,
        String originalName,
        String path,
        Long size,
        String contentType
) {
    public static FileUploadResponse from(FileMetadata fileMetadata) {
        return FileUploadResponse.builder()
                .id(fileMetadata.getId())
                .originalName(fileMetadata.getOriginalName())
                .path(fileMetadata.getPath())
                .size(fileMetadata.getSize())
                .contentType(fileMetadata.getContentType())
                .build();
    }
}