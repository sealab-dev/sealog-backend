package com.sealog.backend.domain.feature.auth.controller;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse.Token;
import com.sealog.backend.domain.feature.auth.service.AuthService;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.security.util.CookieUtil;
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
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * 내 정보 조회
     * GET /api/auth/me
     * user
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
