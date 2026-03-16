package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesMeResponse;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Series (Me)", description = "내 시리즈 관리 API (인증 필요)")
@SecurityRequirement(name = "Bearer Authentication")
public interface SeriesMeControllerDocs {

    @Operation(summary = "내 시리즈 목록 조회", description = "내가 생성한 모든 시리즈 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    PageResponse<SeriesMeResponse.MySeriesItem> getPagedItems(
            CustomUserDetails userDetails,
            Pageable pageable
    );

    @Operation(summary = "내 시리즈 상세/게시글 목록 조회", description = "내 특정 시리즈에 속한 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    PageResponse<SeriesMeResponse.MyPostItem> getPagedPostItems(
            CustomUserDetails userDetails,
            @Parameter(description = "시리즈 slug", example = "spring-series") String slug,
            Pageable pageable
    );

    @Operation(summary = "시리즈 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
    })
    CustomResponse<Void> create(CustomUserDetails userDetails, @Valid SeriesMeRequest.Create request);

    @Operation(summary = "시리즈 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
    })
    CustomResponse<Void> update(CustomUserDetails userDetails, Long seriesId, @Valid SeriesMeRequest.Update request);

    @Operation(summary = "시리즈 공개 처리")
    CustomResponse<Void> show(CustomUserDetails userDetails, Long seriesId);

    @Operation(summary = "시리즈 비공개 처리")
    CustomResponse<Void> hide(CustomUserDetails userDetails, Long seriesId);

    @Operation(summary = "시리즈 삭제")
    CustomResponse<Void> delete(CustomUserDetails userDetails, Long seriesId);
}
