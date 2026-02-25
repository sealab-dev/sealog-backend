package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.entity.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserFileRepository;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.file.repository.FileMetadataRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.service.FileStorageService;
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
    private final UserFileRepository userFileRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final UserValidatorService userValidatorService;

    @Override
    @Transactional
    public User signUp(AuthRequest.SignUpRequest request) {
        // 이메일 중복 검사
        userValidatorService.validateDuplicateEmail(request.getEmail());

        // 닉네임 중복 검사
        userValidatorService.validateDuplicateNickname(request.getNickname());

        // 비밀번호 암호화 및 User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .role(UserRole.USER)
                .build();

        return userRepository.save(user);
    }

    @Override
    public User login(AuthRequest.LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        return user;
    }

    @Override
    public User getUserForRefresh(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.unauthorized("사용자를 찾을 수 없습니다"));
    }

    @Override
    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.unauthorized("사용자를 찾을 수 없습니다"));
        user.updateRefreshToken(refreshToken);
    }

    @Override
    public void validateStoredRefreshToken(Long userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.unauthorized("사용자를 찾을 수 없습니다"));

        String storedToken = user.getRefreshToken();
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }
    }

    @Override
    @Transactional
    public void deleteRefreshToken(Long userId) {
        userRepository.findById(userId).ifPresent(User::clearRefreshToken);
    }

}