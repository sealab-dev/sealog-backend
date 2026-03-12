package com.sealog.backend.domain.feature.auth.controller;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.domain.feature.auth.dto.TokenResponse;
import com.sealog.backend.domain.feature.auth.service.AuthService;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.security.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     * 로그인
     * POST /api/auth/login
     * guest
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<AuthResponse.AuthProfile>> login(
            @Valid @RequestBody AuthRequest.Login request,
            HttpServletResponse response
    ) {
        log.info("[Auth] Login attempt - Email: {}", request.getEmail());
        TokenResponse tokenResponse = authService.login(request);

        cookieUtil.addAccessTokenCookie(response, tokenResponse.getAccessToken());
        cookieUtil.addRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        log.info("[Auth] Login success - UserID: {}", tokenResponse.getAuthProfile().getId());

        return ResponseEntity.ok(CustomResponse.success(tokenResponse.getAuthProfile(), "로그인 성공"));
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
    public ResponseEntity<CustomResponse<AuthResponse.AuthProfile>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.debug("[Auth] Token refresh request received");
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> CustomException.unauthorized("Refresh Token이 없습니다"));

        TokenResponse tokenResponse = authService.refresh(refreshToken);

        log.info("[Auth] Refreshing token for UserID: {}", tokenResponse.getAuthProfile().getId());

        cookieUtil.addAccessTokenCookie(response, tokenResponse.getAccessToken());

        return ResponseEntity.ok(CustomResponse.success(tokenResponse.getAuthProfile(), "토큰이 재발급되었습니다"));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * user
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<CustomResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        cookieUtil.getRefreshToken(request).ifPresent(authService::logout);

        cookieUtil.deleteTokenCookies(response);
        return ResponseEntity.ok(CustomResponse.success(null, "로그아웃 되었습니다"));
    }
}
