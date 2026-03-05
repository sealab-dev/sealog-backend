package com.sealog.backend.domain.feature.archive.controller;

import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
import com.sealog.backend.domain.feature.archive.service.ArchiveService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 아카이브 공개 컨트롤러 (인증 불필요)
 *
 * 역할:
 * - 공개 아카이브 목록 조회 (닉네임 기준)
 * - 아카이브에 속한 공개 게시글 목록 조회
 */
@RestController
@RequestMapping("/api/guest/archive")
@RequiredArgsConstructor
public class ArchiveGuestController implements ArchiveGuestControllerDocs {

    private final ArchiveService archiveService;

    // ========== 조회 ========== //

    /**
     * 사용자의 공개 아카이브 목록 조회
     * GET /api/guest/archive/{nickname}
     */
    @Override
    @GetMapping("/{nickname}")
    public ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getPagedItemsByNicknameForGuest(
            @PathVariable String nickname,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArchiveResponse.ArchiveItems> result = archiveService.getPagedItemsByNicknameForGuest(nickname, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    /**
     * 아카이브에 속한 공개 게시글 목록 조회
     * GET /api/guest/archive/{archiveId}/posts
     */
    @Override
    @GetMapping("/{archiveId}/posts")
    public ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.PostItems>>> getPagedPostItemsByArchiveIdForGuest(
            @PathVariable Long archiveId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArchiveResponse.PostItems> result = archiveService.getPagedPostItemsByArchiveIdForGuest(archiveId, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }
}
