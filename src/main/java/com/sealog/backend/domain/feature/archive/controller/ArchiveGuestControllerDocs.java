package com.sealog.backend.domain.feature.archive.controller;

import com.sealog.backend.domain.feature.archive.dto.ArchivePostResponse;
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

@Tag(name = "Archive", description = "아카이브(모음집) 공개 API (인증 불필요)")
@SecurityRequirements()
public interface ArchiveGuestControllerDocs {

    @Operation(
            summary = "사용자의 공개 아카이브 목록 조회",
            description = "nickname 기준으로 공개 아카이브 목록을 페이징 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getArchivesByNickname(
            @Parameter(description = "사용자 닉네임", example = "테스터") String nickname,
            Pageable pageable
    );

    @Operation(
            summary = "게시글의 공개 아카이브 목록 조회",
            description = "특정 게시글이 담긴 공개 아카이브 목록을 페이징 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<CustomResponse<PageResponse<ArchivePostResponse.ArchivePostItems>>> getArchivePostsByPostId(
            @Parameter(description = "게시글 ID", example = "1") Long postId,
            Pageable pageable
    );
}
