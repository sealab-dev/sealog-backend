package com.sealog.backend.domain.feature.archive.controller;

import com.sealog.backend.domain.feature.archive.dto.ArchivePostResponse;
import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
import com.sealog.backend.domain.feature.archive.service.ArchivePostService;
import com.sealog.backend.domain.feature.archive.service.ArchiveService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 아카이브 공개 컨트롤러 (인증 불필요)
 *
 * - 공개 아카이브 목록 조회 (닉네임 기준)
 * - 게시글의 공개 아카이브 목록 조회
 */
@RestController
@RequestMapping("/api/guest/archive")
@RequiredArgsConstructor
public class ArchiveGuestController implements ArchiveGuestControllerDocs {

    private final ArchiveService archiveService;
    private final ArchivePostService archivePostService;

    /**
     * 사용자의 공개 아카이브 목록 조회
     * GET /api/guest/archive/{nickname}
     */
    @Override
    @GetMapping("/{nickname}")
    public ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getArchivesByNickname(
            @PathVariable String nickname,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArchiveResponse.ArchiveItems> result = archiveService.getPagedItemsByNicknameForGuest(nickname, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    /**
     * 게시글의 공개 아카이브 목록 조회
     * GET /api/guest/archive/post/{postId}
     */
    @Override
    @GetMapping("/post/{postId}")
    public ResponseEntity<CustomResponse<PageResponse<ArchivePostResponse.ArchivePostItems>>> getArchivePostsByPostId(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ArchivePostResponse.ArchivePostItems> result = archivePostService.getPagedPublicItems(postId, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }
}
