package com.sealog.backend.domain.feature.archive.controller;

import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
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
 * Archive 공개 API 문서 인터페이스 (Swagger 전용, 인증 불필요)
 *
 * 역할:
 * - 공개 아카이브 목록 조회 (닉네임 기준)
 * - 아카이브에 속한 공개 게시글 목록 조회
 */
@Tag(name = "Archive", description = "아카이브(모음집) 공개 API (인증 불필요)")
@SecurityRequirements()
public interface ArchiveGuestControllerDocs {

    @Operation(
            summary = "사용자의 공개 아카이브 목록 조회",
            description = "닉네임 기준으로 공개(isPublic=true) 아카이브 목록을 페이징 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getPagedItemsByNicknameForGuest(
            @Parameter(description = "사용자 닉네임", example = "테스터") String nickname,
            Pageable pageable
    );

    @Operation(
            summary = "아카이브에 속한 공개 게시글 목록 조회",
            description = "특정 아카이브에 담긴 PUBLISHED 게시글 목록을 페이징 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.PostItems>>> getPagedPostItemsByArchiveIdForGuest(
            @Parameter(description = "아카이브 ID", example = "1") Long archiveId,
            Pageable pageable
    );
}
