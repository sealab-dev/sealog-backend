package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserAdminRequest;
import com.sealog.backend.global.response.CustomResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User-Admin", description = "사용자 관리 API (Admin 전용)")
@SecurityRequirement(name = "bearerAuth")
public interface UserAdminControllerDocs {

    @Operation(
            summary = "사용자 생성",
            description = "새로운 사용자를 생성합니다. (Admin 전용)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 회원 정보")
    })
    CustomResponse<Void> createUser(@Valid UserAdminRequest.Create request);
}
