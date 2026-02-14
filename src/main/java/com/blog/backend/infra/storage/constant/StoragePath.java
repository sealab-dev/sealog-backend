package com.blog.backend.infra.storage.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoragePath {
    PUBLIC_IMAGE("public/images"),
    PUBLIC_VIDEO("public/videos"),
    PUBLIC_DOCUMENT("public/documents"),
    PUBLIC_AUDIO("public/audios"),
    PUBLIC_ARCHIVE("public/archives"),
    PUBLIC_ASSET("public/assets"),

    /* 프라이빗 경로(아무나 접근하면 안되는 경로에 저장) : 확장 예정 */
    PRIVATE_DOCUMENT("private/documents");

    private final String basePath;
}
