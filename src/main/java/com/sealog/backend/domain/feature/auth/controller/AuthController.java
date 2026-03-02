package com.sealog.backend.domain.feature.auth.controller;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.service.AuthService;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.security.util.CookieUtil;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    /**
     * 로그인
     * POST /api/auth/login
     * guest
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<UserResponse.MyProfile>> login(
            @Valid @RequestBody AuthRequest.LoginRequest request,
            HttpServletResponse response
    ) {
        log.info("[Auth] Login attempt - Email: {}", request.getEmail());
        User user = authService.login(request);

        String refreshToken = setTokenCookies(response, user);
        authService.saveRefreshToken(user.getId(), refreshToken);

        log.info("[Auth] Login success - UserID: {}", user.getId());

        return ResponseEntity.ok(CustomResponse.success(UserResponse.MyProfile.from(user), "로그인 성공"));
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
    public ResponseEntity<CustomResponse<UserResponse.MyProfile>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.debug("[Auth] Token refresh request received");
        // 쿠키에서 Refresh Token 추출
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> CustomException.unauthorized("Refresh Token이 없습니다"));

        // JWT 서명 및 만료 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            cookieUtil.deleteTokenCookies(response);
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        // DB에 저장된 Refresh Token과 비교 검증
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        log.info("[Auth] Refreshing token for UserID: {}", userId);

        authService.validateStoredRefreshToken(userId, refreshToken);

        // 사용자 조회 및 액세스 토큰만 재발급
        User user = authService.getUserForRefresh(userId);
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        cookieUtil.addAccessTokenCookie(response, newAccessToken);

        return ResponseEntity.ok(CustomResponse.success(UserResponse.MyProfile.from(user), "토큰이 재발급되었습니다"));
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
        // Refresh Token이 유효한 경우 DB에서 삭제
        cookieUtil.getRefreshToken(request).ifPresent(refreshToken -> {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                Long userId = jwtTokenProvider.getUserId(refreshToken);
                authService.deleteRefreshToken(userId);
            }
        });

        cookieUtil.deleteTokenCookies(response);
        return ResponseEntity.ok(CustomResponse.success(null, "로그아웃 되었습니다"));
    }

    /**
     * 액세스 + 리프레시 토큰 생성 및 쿠키 설정
     * @return 생성된 Refresh Token (DB 저장용)
     */
    private String setTokenCookies(HttpServletResponse response, User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        return refreshToken;
    }
}