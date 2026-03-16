package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesMeResponse;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.service.SeriesService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Series (Me)", description = "내 시리즈 관리 API (인증 필요)")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/me/series")
@RequiredArgsConstructor
public class SeriesMeController {

    private final SeriesService seriesService;

    @Operation(summary = "내 시리즈 목록 조회", description = "내가 생성한 모든 시리즈 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesMeResponse.MySeriesItem> getPagedItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesMeResponse.MySeriesItem> items = seriesService.getPagedItems(userDetails.getUserId(), pageable);
        return PageResponse.from(items);
    }

    @Operation(summary = "내 시리즈 상세/게시글 목록 조회", description = "내 특정 시리즈에 속한 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesMeResponse.MyPostItem> getPagedPostItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "시리즈 slug", example = "spring-series") @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesMeResponse.MyPostItem> items = seriesService.getPagedPostItemsMe(userDetails.getUserId(), slug, pageable);
        return PageResponse.from(items);
    }

    @Operation(summary = "시리즈 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomResponse<Void> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SeriesMeRequest.Create request
    ) {
        seriesService.create(userDetails.getUserId(), request);
        return CustomResponse.success("시리즈가 생성되었습니다");
    }

    @Operation(summary = "시리즈 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
    })
    @PutMapping("/{seriesId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse<Void> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId,
            @Valid @RequestBody SeriesMeRequest.Update request
    ) {
        seriesService.update(userDetails.getUserId(), seriesId, request);
        return CustomResponse.success("시리즈 정보가 수정되었습니다");
    }

    @Operation(summary = "시리즈 공개 처리")
    @PatchMapping("/{seriesId}/show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> show(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId
    ) {
        seriesService.show(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 공개로 전환되었습니다");
    }

    @Operation(summary = "시리즈 비공개 처리")
    @PatchMapping("/{seriesId}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> hide(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId
    ) {
        seriesService.hide(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 비공개로 전환되었습니다");
    }

    @Operation(summary = "시리즈 삭제")
    @DeleteMapping("/{seriesId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long seriesId
    ) {
        seriesService.delete(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 삭제되었습니다");
    }
}
