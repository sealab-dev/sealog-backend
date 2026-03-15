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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeriesController implements SeriesControllerDocs {

    private final SeriesService seriesService;

    @Override
    @GetMapping("/{nickname}/series")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesResponse.SeriesItem> getPagedPublicItems(
            @PathVariable String nickname,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.SeriesItem> items = seriesService.getPagedPublicItems(nickname, pageable);
        return PageResponse.from(items);
    }

    @Override
    @GetMapping("/{nickname}/series/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesResponse.PostItem> getPagedPostItems(
            @PathVariable String nickname,
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.PostItem> items = seriesService.getPagedPostItemsByNickname(nickname, slug, pageable);
        return PageResponse.from(items);
    }
}
