package com.sealog.backend.domain.feature.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 발급 결과 DTO
 * - 로그인: accessToken, refreshToken, user 모두 포함
 * - 재발급: accessToken, user만 포함 (refreshToken은 null)
 */
@Getter
@Builder
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;
    private final AuthResponse.AuthProfile authProfile;
}
