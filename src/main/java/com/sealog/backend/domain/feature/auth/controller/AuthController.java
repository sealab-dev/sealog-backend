package com.sealog.backend.domain.feature.auth.controller;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse.Token;
import com.sealog.backend.domain.feature.auth.service.AuthService;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.security.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API
 * - 토큰은 HttpOnly 쿠키로 전달
 */
@Slf4j
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * 내 정보 조회
     * GET /api/auth/me
     * user
     */
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 인증 프로필을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse.AuthProfile getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return authService.getMe(userDetails.getUserId());
    }

    /**
     * 로그인
     * POST /api/auth/login
     * guest
     */
    @Operation(summary = "로그인", description = "로그인 후 JWT 발급 (HttpOnly 쿠키로 전달)")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse.AuthProfile login(
            @Valid @RequestBody AuthRequest.Login request,
            HttpServletResponse response
    ) {
        log.info("[Auth] Login attempt - Email: {}", request.getEmail());
        Token token = authService.login(request);

        cookieUtil.addAccessTokenCookie(response, token.getAccessToken());
        cookieUtil.addRefreshTokenCookie(response, token.getRefreshToken());

        log.info("[Auth] Login success - UserID: {}", token.getAuthProfile().getId());

        return token.getAuthProfile();
    }

    /**
     * 토큰 재발급
     * POST /api/auth/refresh
     * - Refresh Token은 쿠키에서 자동으로 추출
     * guest
     * - 리프레시 토큰 로테이션 없음: 액세스 토큰만 재발급
     */
    @Operation(summary = "토큰 재발급", description = "쿠키의 Refresh Token을 DB와 비교 검증 후 Access Token만 재발급 (Refresh Token 로테이션 없음)")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 없음/유효하지 않음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse.AuthProfile refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.debug("[Auth] Token refresh request received");
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> CustomException.unauthorized("Refresh Token이 없습니다"));

        Token token = authService.refresh(refreshToken);

        log.info("[Auth] Refreshing token for UserID: {}", token.getAuthProfile().getId());

        cookieUtil.addAccessTokenCookie(response, token.getAccessToken());

        return token.getAuthProfile();
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * user
     */
    @Operation(summary = "로그아웃", description = "DB에서 Refresh Token 삭제 및 토큰 쿠키 만료 처리")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        cookieUtil.getRefreshToken(request).ifPresent(authService::logout);

        cookieUtil.deleteTokenCookies(response);
    }
}
