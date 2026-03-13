package com.sealog.backend.domain.feature.archive.controller;

import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
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
 * 역할:
 * - 아카이브 목록·게시글 조회
 * - 아카이브 CRUD
 * - 아카이브 공개/비공개
 * - 게시글-아카이브 배정/해제
 */
@RestController
@RequestMapping("/api/user/archive")
@RequiredArgsConstructor
public class ArchiveUserController implements ArchiveUserControllerDocs {

    private final ArchiveService archiveService;

    // ========== 조회 ========== //

    /**
     * 내 아카이브 목록 조회
     * GET /api/user/archive
     */
    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getPagedItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArchiveResponse.ArchiveItems> result = archiveService.getPagedItems(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    /**
     * 내 아카이브 게시글 목록 조회
     * GET /api/user/archive/{nickname}/{slug}/posts
     */
    @Override
    @GetMapping("/{nickname}/{slug}/posts")
    public ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.PostItems>>> getPagedPostItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArchiveResponse.PostItems> result = archiveService.getPagedPostItems(
                userDetails.getUserId(), nickname, slug, pageable
        );
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    // ========== CRUD ========== //

    /**
     * 아카이브 생성
     * POST /api/user/archive
     */
    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ArchiveRequest.Create request
    ) {
        archiveService.create(userDetails.getUserId(), request);
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
    public ResponseEntity<CustomResponse<Void>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug,
            @RequestBody @Valid ArchiveRequest.Update request
    ) {
        archiveService.update(userDetails.getUserId(), nickname, slug, request);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 수정되었습니다"));
    }

    /**
     * 아카이브 공개
     * PATCH /api/user/archive/{nickname}/{slug}/show
     */
    @Override
    @PatchMapping("/{nickname}/{slug}/show")
    public ResponseEntity<CustomResponse<Void>> show(
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
    public ResponseEntity<CustomResponse<Void>> hide(
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
    public ResponseEntity<CustomResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug
    ) {
        archiveService.delete(userDetails.getUserId(), nickname, slug);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 삭제되었습니다"));
    }

    // ========== 게시글 연결 ========== //

    /**
     * 게시글 아카이브 배정
     * PATCH /api/user/archive/{nickname}/{slug}/post/{postId}
     */
    @Override
    @PatchMapping("/{nickname}/{slug}/post/{postId}")
    public ResponseEntity<CustomResponse<Void>> changePostArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String nickname,
            @PathVariable String slug,
            @PathVariable Long postId
    ) {
        archiveService.changePostArchive(userDetails.getUserId(), postId, nickname, slug);
        return ResponseEntity.ok(CustomResponse.success(null, "게시글이 아카이브에 배정되었습니다"));
    }

    /**
     * 게시글 아카이브 해제
     * DELETE /api/user/archive/post/{postId}
     */
    @Override
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<CustomResponse<Void>> deletePostArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        archiveService.deletePostArchive(userDetails.getUserId(), postId);
        return ResponseEntity.ok(CustomResponse.success(null, "게시글 아카이브 배정이 해제되었습니다"));
    }
}