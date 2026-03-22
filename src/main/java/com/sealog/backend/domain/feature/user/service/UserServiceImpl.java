package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.UserAdminRequest;
import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.dto.UserMeResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 사용자 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidatorService userValidatorService;
    private final UserFileService userFileService;
    private final UserSocialService userSocialService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    public UserMeResponse.MyProfile getMyProfile(Long userId) {
        User user = findUserById(userId);

        List<UserMeResponse.MySocialLinkItem> socialLinks = userSocialService.getMyLinks(userId);
        return UserMeResponse.MyProfile.of(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPosition(),
                user.getAbout(),
                fileStorageService.getFileUrl(user.getProfileImagePath()),
                socialLinks
        );
    }

    @Override
    @Transactional
    public UserMeResponse.MyProfile updateProfile(
            Long userId,
            UserMeRequest.UpdateProfile request,
            MultipartFile profileImage
    ) {
        User user = findUserById(userId);

        // 닉네임 변경 시 중복 검증
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            if (!user.getNickname().equals(request.getNickname())) {
                userValidatorService.validateDuplicateNickname(request.getNickname());
                user.updateNickname(request.getNickname());
            }
        }

        if (request.getPosition() != null) {
            user.updatePosition(request.getPosition());
        }

        if (request.getAbout() != null) {
            user.updateAbout(request.getAbout());
        }

        handleProfileImage(user, request, profileImage);

        if (request.getSocialLinks() != null) {
            userSocialService.update(userId, request.getSocialLinks());
        }

        userRepository.save(user);

        return getMyProfile(userId);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UserMeRequest.UpdatePassword request) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw CustomException.badRequest("현재 비밀번호가 일치하지 않습니다");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw CustomException.badRequest("새 비밀번호가 일치하지 않습니다");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw CustomException.badRequest("새 비밀번호는 현재 비밀번호와 달라야 합니다");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponse.UserProfile getPublicProfile(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        List<UserResponse.SocialLinkItem> socialLinks = userSocialService.getPublicLinks(nickname);
        return UserResponse.UserProfile.of(
                user.getNickname(),
                user.getPosition(),
                fileStorageService.getFileUrl(user.getProfileImagePath()),
                user.getAbout(),
                socialLinks
        );
    }

    @Override
    @Transactional
    public void createUser(UserAdminRequest.Create request) {
        userValidatorService.validateDuplicateEmail(request.getEmail());
        userValidatorService.validateDuplicateNickname(request.getNickname());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    // ========== Private 보조 메소드 ========== //

    /**
     * ID 기반 사용자 조회 (내부용)
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));
    }

    /**
     * 프로필 이미지 처리 로직
     * - 이미지 삭제 요청 시 기존 파일 및 경로 제거
     * - 새 이미지 업로드 시 기존 이미지 교체
     */
    private void handleProfileImage(User user, UserMeRequest.UpdateProfile request, MultipartFile profileImage) {
        if (Boolean.TRUE.equals(request.getRemoveProfileImage())) {
            userFileService.deleteProfile(user.getId());
            user.removeProfileImage();
            return;
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            userFileService.deleteProfile(user.getId());
            String path = userFileService.uploadAndSaveProfile(user, profileImage);
            user.updateProfileImagePath(path);
        }
    }
}
