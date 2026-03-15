package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Series", description = "공개 시리즈 API (인증 불필요)")
@SecurityRequirements()
public interface SeriesControllerDocs {

    @Operation(summary = "유저의 공개 시리즈 목록 조회", description = "nickname 기준으로 공개된 시리즈 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    PageResponse<SeriesResponse.SeriesItem> getPagedPublicItems(
            @Parameter(description = "사용자 닉네임", example = "테스터") String nickname,
            Pageable pageable
    );

    @Operation(summary = "시리즈 내 공개 게시글 목록 조회", description = "특정 시리즈에 속한 PUBLISHED 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    PageResponse<SeriesResponse.PostItem> getPagedPostItems(
            @Parameter(description = "사용자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "시리즈 slug", example = "spring-series") String slug,
            Pageable pageable
    );
}
