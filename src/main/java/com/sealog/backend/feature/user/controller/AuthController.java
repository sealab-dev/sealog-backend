package com.sealog.backend.feature.user.controller;

import com.sealog.backend.feature.user.dto.AuthRequest;
import com.sealog.backend.feature.user.service.AuthService;
import com.sealog.backend.feature.user.entity.User;
import com.sealog.backend.global.core.response.CustomResponse;
import com.sealog.backend.global.core.exception.CustomException;
import com.sealog.backend.global.security.util.CookieUtil;
import com.sealog.backend.global.security.jwt.JwtTokenProvider;
import com.sealog.backend.feature.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API
 * - 토큰은 HttpOnly 쿠키로 전달
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs{

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    /**
     * 로그인
     * POST /api/auth/login
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<UserResponse.UserInfo>> login(
            @Valid @RequestBody AuthRequest.LoginRequest request,
            HttpServletResponse response
    ) {
        User user = authService.login(request);

        setTokenCookies(response, user);

        return ResponseEntity.ok(CustomResponse.success(UserResponse.UserInfo.from(user), "로그인 성공"));
    }

    /**
     * 토큰 재발급
     * POST /api/auth/refresh
     * - Refresh Token은 쿠키에서 자동으로 추출
     */
    @Override
    @PostMapping("/refresh")
    public ResponseEntity<CustomResponse<UserResponse.UserInfo>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> CustomException.unauthorized("Refresh Token이 없습니다"));

        // 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            cookieUtil.deleteTokenCookies(response);
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        // 사용자 조회
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = authService.getUserForRefresh(userId);

        // 새 토큰 생성 및 쿠키 설정
        setTokenCookies(response, user);

        return ResponseEntity.ok(CustomResponse.success(UserResponse.UserInfo.from(user), "토큰이 재발급되었습니다"));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<CustomResponse<Void>> logout(HttpServletResponse response) {
        cookieUtil.deleteTokenCookies(response);
        return ResponseEntity.ok(CustomResponse.success(null, "로그아웃 되었습니다"));
    }

    /**
     * 토큰 생성 및 쿠키 설정
     */
    private void setTokenCookies(HttpServletResponse response, User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
    }
}