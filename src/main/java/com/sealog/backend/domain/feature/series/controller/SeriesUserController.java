package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesRequest;
import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.service.SeriesService;
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
@RequestMapping("/api/me/series")
@RequiredArgsConstructor
public class SeriesUserController implements SeriesUserControllerDocs {

    private final SeriesService seriesService;

    // ========== 조회 ========== //

    /**
     * 내 아카이브 목록 조회
     * GET /api/me/series
     */
    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<SeriesResponse.SeriesItems>>> getPagedItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.SeriesItems> result = seriesService.getPagedItems(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    /**
     * 내 아카이브 게시글 목록 조회
     * GET /api/me/series/{slug}/posts
     */
    @Override
    @GetMapping("/{slug}/posts")
    public ResponseEntity<CustomResponse<PageResponse<SeriesResponse.PostItems>>> getPagedPostItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.PostItems> result = seriesService.getPagedPostItems(
                userDetails.getUserId(), userDetails.getUser().getNickname(), slug, pageable
        );
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    // ========== CRUD ========== //

    /**
     * 아카이브 생성
     * POST /api/me/series
     */
    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid SeriesRequest.Create request
    ) {
        seriesService.create(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(null, "아카이브가 생성되었습니다"));
    }

    /**
     * 아카이브 수정
     * PUT /api/me/series/{seriesId}
     */
    @Override
    @PutMapping("/{seriesId}")
    public ResponseEntity<CustomResponse<Void>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId,
            @RequestBody @Valid SeriesRequest.Update request
    ) {
        seriesService.update(userDetails.getUserId(), seriesId, request);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 수정되었습니다"));
    }

    /**
     * 아카이브 공개
     * PATCH /api/user/series/{seriesId}/show
     */
    @Override
    @PatchMapping("/{seriesId}/show")
    public ResponseEntity<CustomResponse<Void>> show(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId
    ) {
        seriesService.show(userDetails.getUserId(), seriesId);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 공개되었습니다"));
    }

    /**
     * 아카이브 비공개
     * PATCH /api/user/series/{seriesId}/hide
     */
    @Override
    @PatchMapping("/{seriesId}/hide")
    public ResponseEntity<CustomResponse<Void>> hide(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId
    ) {
        seriesService.hide(userDetails.getUserId(), seriesId);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 비공개되었습니다"));
    }

    /**
     * 아카이브 삭제
     * DELETE /api/user/series/{seriesId}
     */
    @Override
    @DeleteMapping("/{seriesId}")
    public ResponseEntity<CustomResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId
    ) {
        seriesService.delete(userDetails.getUserId(), seriesId);
        return ResponseEntity.ok(CustomResponse.success(null, "아카이브가 삭제되었습니다"));
    }
}