package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesMeResponse;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/series")
@RequiredArgsConstructor
public class SeriesMeController implements SeriesMeControllerDocs {

    private final SeriesService seriesService;

    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesMeResponse.MySeriesItem> getPagedItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesMeResponse.MySeriesItem> items = seriesService.getPagedItems(userDetails.getUserId(), pageable);
        return PageResponse.from(items);
    }

    @Override
    @GetMapping("/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesMeResponse.MyPostItem> getPagedPostItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesMeResponse.MyPostItem> items = seriesService.getPagedPostItemsMe(userDetails.getUserId(), slug, pageable);
        return PageResponse.from(items);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomResponse<Void> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SeriesMeRequest.Create request
    ) {
        seriesService.create(userDetails.getUserId(), request);
        return CustomResponse.success("시리즈가 생성되었습니다");
    }

    @Override
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

    @Override
    @PatchMapping("/{seriesId}/show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> show(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long seriesId) {
        seriesService.show(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 공개로 전환되었습니다");
    }

    @Override
    @PatchMapping("/{seriesId}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> hide(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long seriesId) {
        seriesService.hide(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 비공개로 전환되었습니다");
    }

    @Override
    @DeleteMapping("/{seriesId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long seriesId) {
        seriesService.delete(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 삭제되었습니다");
    }
}
