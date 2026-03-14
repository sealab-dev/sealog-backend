package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.service.SeriesService;
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
 * - 아카이브에 속한 공개 게시글 목록 조회
 */
@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
public class SeriesGuestController implements SeriesGuestControllerDocs {

    private final SeriesService seriesService;

    // ========== 조회 ========== //

    /**
     * 특정 사용자의 공개 아카이브 목록 조회
     * GET /api/guest/{nickname}/series
     */
    @Override
    @GetMapping("/{nickname}/series")
    public ResponseEntity<CustomResponse<PageResponse<SeriesResponse.SeriesItems>>> getPagedPublicItems(
            @PathVariable String nickname,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.SeriesItems> result = seriesService.getPagedPublicItems(nickname, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }

    /**
     * 아카이브에 속한 공개 게시글 목록 조회
     * GET /api/guest/series/{nickname}/{slug}/posts
     */
    @Override
    @GetMapping("/{nickname}/series/{slug}/posts")
    public ResponseEntity<CustomResponse<PageResponse<SeriesResponse.PostItems>>> getPagedPostItems(
            @PathVariable String nickname,
            @PathVariable String slug,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.PostItems> result = seriesService.getPagedPostItems(null, nickname, slug, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(result)));
    }
}
