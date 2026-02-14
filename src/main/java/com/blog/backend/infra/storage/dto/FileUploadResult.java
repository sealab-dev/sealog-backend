package com.blog.backend.infra.storage.dto;

import lombok.Builder;

@Builder
public record FileUploadResult(
        String originalName,
        String path,
        String contentType,
        long fileSize
) {
}
