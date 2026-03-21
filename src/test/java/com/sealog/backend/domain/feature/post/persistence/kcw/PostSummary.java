package com.sealog.backend.domain.feature.post.persistence.kcw;

import com.sealog.backend.domain.feature.post.enums.PostStatus;

public record PostSummary(
        Long id,
        String title,
        String excerpt,
        String slug,
        PostStatus status,
        String thumbnailPath
) {
}
