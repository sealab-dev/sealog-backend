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
import org.springframework.http.ResponseEntity;

/**
 * Series 공개 API 문서 인터페이스 (Swagger 전용, 인증 불필요)
 *
 * 역할:
 * - 아카이브에 속한 공개 게시글 목록 조회
 */
@Tag(name = "Series", description = "아카이브(모음집) 공개 API (인증 불필요)")
@SecurityRequirements()
public interface SeriesGuestControllerDocs {

    @Operation(
            summary = "공개 아카이브 목록 조회",
            description = "특정 사용자의 공개된 아카이브(모음집) 목록을 페이징 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CustomResponse<PageResponse<SeriesResponse.SeriesItems>>> getPagedPublicItems(
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            Pageable pageable
    );

    @Operation(
            summary = "아카이브에 속한 공개 게시글 목록 조회",
            description = "특정 아카이브에 담긴 PUBLISHED 게시글 목록을 페이징 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CustomResponse<PageResponse<SeriesResponse.PostItems>>> getPagedPostItems(
            @Parameter(description = "아카이브 소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug,
            Pageable pageable
    );
}
