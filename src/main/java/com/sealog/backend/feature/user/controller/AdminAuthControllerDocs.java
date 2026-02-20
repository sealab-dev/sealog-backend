package com.sealog.backend.feature.user.controller;

import com.sealog.backend.feature.user.dto.AuthRequest;
import com.sealog.backend.global.core.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin - Auth", description = "어드민 인증 API")
public interface AdminAuthControllerDocs {

    @Operation(summary = "어드민 회원가입", description = "회원가입 처리")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "중복 이메일/닉네임 등",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<Void>> signUp(AuthRequest.SignUpRequest request);
}