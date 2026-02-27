package com.sealog.backend.domain.feature.achieve.service;

import com.sealog.backend.domain.feature.achieve.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.achieve.dto.ArchiveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 아카이브 서비스 인터페이스
 */
public interface ArchiveService {

    // ========== Guest (공개) ========== //

    Page<ArchiveResponse.ArchiveItems> getPagedItems(Pageable pageable);

    // ========== User (인증) ========== //

    Page<ArchiveResponse.ArchiveItems> getPagedItemsForUser(Long userId, Pageable pageable);

    void add(Long userId, ArchiveRequest.Add request);

    void edit(Long userId, String nickname, String slug, ArchiveRequest.Edit request);

    void show(Long userId, String nickname, String slug);

    void hide(Long userId, String nickname, String slug);

    void remove(Long userId, String nickname, String slug);
}
