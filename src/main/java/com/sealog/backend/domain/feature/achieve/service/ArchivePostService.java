package com.sealog.backend.domain.feature.achieve.service;

import com.sealog.backend.domain.feature.achieve.dto.ArchivePostRequest;
import com.sealog.backend.domain.feature.achieve.dto.ArchivePostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 게시글 아카이브 서비스 인터페이스
 */
public interface ArchivePostService {

    // ========== Guest (공개) ========== //

    Page<ArchivePostResponse.ArchivePostItems> getPagedPublicItems(Long postId, Pageable pageable);

    // ========== User (인증) ========== //

    void add(Long userId, ArchivePostRequest.Add request);

    void remove(Long userId, Long archivePostId);
}
