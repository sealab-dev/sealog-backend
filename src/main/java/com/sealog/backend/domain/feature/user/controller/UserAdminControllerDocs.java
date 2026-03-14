package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.global.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User-Admin", description = "사용자 관리 API (Admin 전용)")
@SecurityRequirement(name = "bearerAuth")
public interface UserAdminControllerDocs {

    @Operation(
            summary = "사용자 생성",
            description = "새로운 사용자를 생성합니다. (Admin 전용)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (Admin 아님)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 닉네임",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> createUser(UserRequest.Create request);
}
