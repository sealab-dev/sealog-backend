package com.sealog.backend.domain.feature.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {

    PUBLISHED("PUBLISHED", "공개"),
    PRIVATE("PRIVATE", "비공개"),
    DRAFT("DRAFT", "작성 중");

    private final String key;
    private final String title;

    public static PostStatus fromKey(String key) {
        for (PostStatus status : values()) {
            if (status.key.equalsIgnoreCase(key)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + key);
    }
}