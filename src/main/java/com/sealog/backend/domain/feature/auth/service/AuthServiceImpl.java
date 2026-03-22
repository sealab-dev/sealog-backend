package com.sealog.backend.domain.feature.auth.service;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.domain.feature.auth.store.RefreshTokenStore;
import com.sealog.backend.infra.storage.service.FileStorageService;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.user.service.UserValidatorService;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileStorageService fileStorageService;
    private final RefreshTokenStore refreshTokenStore;

    @Override
    public AuthResponse.AuthProfile getMe(Long userId) {
        User user = getUserById(userId);
        return buildAuthProfile(user);
    }

    @Override
    @Transactional
    public AuthResponse.Token login(AuthRequest.Login request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> CustomException.badRequest("이메일 또는 비밀번호가 일치하지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw CustomException.badRequest("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        refreshTokenStore.save(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity());

        return AuthResponse.Token.of(accessToken, refreshToken, buildAuthProfile(user));
    }

    @Override
    public AuthResponse.Token refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = getUserById(userId);

        String stored = refreshTokenStore.find(userId)
                .orElseThrow(() -> CustomException.unauthorized("유효하지 않은 Refresh Token입니다"));
        if (!stored.equals(refreshToken)) {
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        return AuthResponse.Token.of(newAccessToken, null, buildAuthProfile(user));
    }

    @Override
    public void logout(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            refreshTokenStore.delete(userId);
        }
    }

    // ========== Private 보조 메소드 ========== //

    /**
     * ID 기반 사용자 조회
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));
    }

    /**
     * User 엔티티를 AuthProfile DTO로 변환
     */
    private AuthResponse.AuthProfile buildAuthProfile(User user) {
        return AuthResponse.AuthProfile.of(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getRole(),
                fileStorageService.getFileUrl(user.getProfileImagePath())
        );
    }
}
