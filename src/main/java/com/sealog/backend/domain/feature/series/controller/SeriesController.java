package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.service.SeriesService;
import com.sealog.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Series", description = "공개 시리즈 API (인증 불필요)")
@SecurityRequirements()
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @Operation(
            summary = "유저의 공개 시리즈 목록 조회",
            description = "닉네임 기준으로 공개된 시리즈 목록을 페이지 조회합니다. 응답 data: `PageResponse<SeriesItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/{nickname}/series")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesResponse.SeriesItem> getPagedPublicItems(
            @Parameter(description = "사용자 닉네임", example = "seadev") @PathVariable String nickname,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.SeriesItem> items = seriesService.getPagedPublicItems(nickname, pageable);
        return PageResponse.from(items);
    }

    @Operation(
            summary = "시리즈 내 공개 게시글 목록 조회",
            description = "특정 시리즈에 속한 PUBLISHED 게시글 목록을 페이지 조회합니다. 응답 data: `PageResponse<SeriesPostItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "시리즈 또는 사용자 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/{nickname}/series/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesResponse.SeriesPostItem> getPagedPostItems(
            @Parameter(description = "사용자 닉네임", example = "seadev") @PathVariable String nickname,
            @Parameter(description = "시리즈 슬러그", example = "spring-series") @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesResponse.SeriesPostItem> items = seriesService.getPagedPostItemsByNickname(nickname, slug, pageable);
        return PageResponse.from(items);
    }
}
