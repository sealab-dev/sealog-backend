package com.sealog.backend.domain.feature.archive.controller;

import com.sealog.backend.domain.feature.archive.dto.ArchivePostRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchivePostResponse;
import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
import com.sealog.backend.domain.feature.archive.service.ArchivePostService;
import com.sealog.backend.domain.feature.archive.service.ArchiveService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 아카이브 컨트롤러 (인증 필수)
 *
 * - 아카이브 CRUD
 * - 게시글-아카이브 연결/해제
 */
@RestController
@RequestMapping("/api/user/archive")
@RequiredArgsConstructor
public class ArchiveUserController implements ArchiveUserControllerDocs {

    private final ArchiveService archiveService;
    private final ArchivePostService archivePostService;

    // ========== Archive CRUD ========== //

    /**
     * 내 아카이브 목록 조회
     * GET /api/user/archive
     */
    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getMyArchives(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArchiveResponse.ArchiveItems> result = archiveService.getPagedItemsForUser(
                userDetails.getUserId(), pageable
        );
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    /**
     * 아카이브 생성
     * POST /api/user/archive
     */
    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> createArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ArchiveRequest.Add request
    ) {
        archiveService.add(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(null, "아카이브가 생성되었습니다"));
    }

    /**
     * 아카이브 수정
     * PUT /api/user/archive/{nickname}/{slug}
     */
    @Override
    @PutMapping("/{nickname}/{slug}")
    public ResponseEntity<CustomResponse<Void>> updateArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug,
            @RequestBody @Valid ArchiveRequest.Edit request
    ) {
        archiveService.edit(userDetails.getUserId(), nickname, slug, request);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 수정되었습니다"));
    }

    /**
     * 아카이브 공개
     * PATCH /api/user/archive/{nickname}/{slug}/show
     */
    @Override
    @PatchMapping("/{nickname}/{slug}/show")
    public ResponseEntity<CustomResponse<Void>> showArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug
    ) {
        archiveService.show(userDetails.getUserId(), nickname, slug);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 공개되었습니다"));
    }

    /**
     * 아카이브 비공개
     * PATCH /api/user/archive/{nickname}/{slug}/hide
     */
    @Override
    @PatchMapping("/{nickname}/{slug}/hide")
    public ResponseEntity<CustomResponse<Void>> hideArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug
    ) {
        archiveService.hide(userDetails.getUserId(), nickname, slug);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 비공개되었습니다"));
    }

    /**
     * 아카이브 삭제
     * DELETE /api/user/archive/{nickname}/{slug}
     */
    @Override
    @DeleteMapping("/{nickname}/{slug}")
    public ResponseEntity<CustomResponse<Void>> deleteArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug
    ) {
        archiveService.remove(userDetails.getUserId(), nickname, slug);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 삭제되었습니다"));
    }

    // ========== ArchivePost ========== //

    /**
     * 게시글 아카이브 추가
     * POST /api/user/archive/post
     */
    @Override
    @PostMapping("/post")
    public ResponseEntity<CustomResponse<Void>> addArchivePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ArchivePostRequest.Add request
    ) {
        archivePostService.add(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(null, "게시글이 아카이브에 추가되었습니다"));
    }

    /**
     * 게시글 아카이브 제거
     * DELETE /api/user/archive/post/{archivePostId}
     */
    @Override
    @DeleteMapping("/post/{archivePostId}")
    public ResponseEntity<CustomResponse<Void>> removeArchivePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long archivePostId
    ) {
        archivePostService.remove(userDetails.getUserId(), archivePostId);
        return ResponseEntity.ok(CustomResponse.success(null, "게시글이 아카이브에서 제거되었습니다"));
    }
}
