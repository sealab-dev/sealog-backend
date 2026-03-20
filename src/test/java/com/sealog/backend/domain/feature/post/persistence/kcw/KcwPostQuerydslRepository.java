package com.sealog.backend.domain.feature.post.persistence.kcw;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KcwPostQuerydslRepository {

    Page<PostSummary> searchLike(String nickname, String keyword, Pageable pageable);

    Page<PostSummary> searchFt(String nickname, String keyword, Pageable pageable);
}
