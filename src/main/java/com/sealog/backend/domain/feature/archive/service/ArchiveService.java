package com.sealog.backend.domain.feature.archive.service;

import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 아카이브 서비스 인터페이스
 */
public interface ArchiveService {

    // ========== Guest (공개) ========== //

    Page<ArchiveResponse.ArchiveItems> getPagedPublicItemsByNickname(String nickname, Pageable pageable);

    Page<ArchiveResponse.PostItems> getPagedPostItemsByArchiveIdForGuest(Long archiveId, Pageable pageable);

    // ========== User (인증) ========== //

    Page<ArchiveResponse.ArchiveItems> getPagedItemsForUser(Long userId, Pageable pageable);

    Page<ArchiveResponse.PostItems> getPagedPostItemsByUserIdAndArchiveIdForUser(Long userId, Long archiveId, Pageable pageable);

    void add(Long userId, ArchiveRequest.Add request);

    void edit(Long userId, String nickname, String slug, ArchiveRequest.Edit request);

    void show(Long userId, String nickname, String slug);

    void hide(Long userId, String nickname, String slug);

    void remove(Long userId, String nickname, String slug);

    void changePostArchive(Long userId, Long archiveId, Long postId);

}
