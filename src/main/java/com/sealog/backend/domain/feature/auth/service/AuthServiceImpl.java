package com.sealog.backend.domain.feature.auth.service;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.infra.storage.service.FileStorageService;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.user.service.UserValidatorService;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidatorService userValidatorService;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileStorageService fileStorageService;

    @Override
    public AuthResponse.AuthProfile getMe(Long userId) {
        User user = getUserById(userId);
        return AuthResponse.AuthProfile.from(user, fileStorageService.getFileUrl(user.getProfileImagePath()));
    }

    @Override
    @Transactional
    public AuthResponse.Token login(AuthRequest.Login request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        //  Refresh Token DB 저장
        user.updateRefreshToken(refreshToken);

        AuthResponse.AuthProfile authProfile = AuthResponse.AuthProfile.from(user, fileStorageService.getFileUrl(user.getProfileImagePath()));

        return AuthResponse.Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authProfile(authProfile)
                .build();
    }

    @Override
    public AuthResponse.Token refresh(String refreshToken) {
        // JWT 서명 및 만료 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        // userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = getUserById(userId);

        // DB 저장 토큰과 비교
        validateStoredRefreshToken(user, refreshToken);

        // 새 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        AuthResponse.AuthProfile authProfile = AuthResponse.AuthProfile.from(user, fileStorageService.getFileUrl(user.getProfileImagePath()));

        return AuthResponse.Token.builder()
                .accessToken(newAccessToken)
                .authProfile(authProfile)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // 유효한 경우에만 userId 추출 → DB에서 삭제
        if (jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            userRepository.findById(userId).ifPresent(User::clearRefreshToken);
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.unauthorized("사용자를 찾을 수 없습니다"));
    }

    private void validateStoredRefreshToken(User user, String refreshToken) {
        String storedToken = user.getRefreshToken();
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }
    }
}
