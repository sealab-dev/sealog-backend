package com.sealog.backend.domain.feature.collections.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Archive", description = "아카이브(모음집) 관리 API")
@SecurityRequirements()
public interface ArchiveGuestControllerDocs {

    /**
     * 아카이브 조회
     **/
    @Operation(
            summary = "사용자의 아카이브 목록 조회",
            description = "nickname 기준으로 아카이브 목록을 조회합니다. 공개 아카이브는 누구나, 비공개 아카이브는 소유자만 목록에 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음"
            )
    })
    void getArchivesList(
            @Parameter(description = "사용자 닉네임", example = "테스터")
            String nickname
    );

}