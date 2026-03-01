package com.sealog.backend.domain.feature.auth.controller;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.global.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerDocs {

    @Operation(summary = "로그인", description = "로그인 후 JWT 발급 (HttpOnly 쿠키로 전달)")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<UserResponse.MyProfile>> login(
            AuthRequest.LoginRequest request,
            HttpServletResponse response
    );

    @Operation(summary = "토큰 재발급", description = "쿠키의 Refresh Token을 DB와 비교 검증 후 Access Token만 재발급 (Refresh Token 로테이션 없음)")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 없음/유효하지 않음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<UserResponse.MyProfile>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    );

    @Operation(summary = "로그아웃", description = "DB에서 Refresh Token 삭제 및 토큰 쿠키 만료 처리")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
    })
    ResponseEntity<CustomResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response);
}