package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidatorService userValidatorService;
    private final UserFileService userFileService;
    private final UserSocialService userSocialService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Value("${storage.base-url}")
    private String storageBaseUrl;

    @Override
    public UserResponse.MyProfile getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        List<UserResponse.SocialLinkItem> socialLinks = userSocialService.getMyLinks(userId);
        return UserResponse.MyProfile.of(user, fileStorageService.getFileUrl(user.getProfileImagePath()), socialLinks);
    }

    @Override
    @Transactional
    public UserResponse.MyProfile updateProfile(
            Long userId,
            UserRequest.UpdateProfile request,
            MultipartFile profileImage
    ) {
        log.info("프로필 수정 시작: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

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
        log.info("프로필 수정 완료: userId={}", userId);

        List<UserResponse.SocialLinkItem> socialLinks = userSocialService.getMyLinks(userId);
        return UserResponse.MyProfile.of(user, fileStorageService.getFileUrl(user.getProfileImagePath()), socialLinks);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UserRequest.UpdatePassword request) {
        log.info("비밀번호 변경 시작: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

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
        user.clearRefreshToken();
        userRepository.save(user);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    @Override
    public UserResponse.PublicProfile getPublicProfile(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        List<UserResponse.SocialLinkItem> socialLinks = userSocialService.getPublicLinks(nickname);
        return UserResponse.PublicProfile.of(user, fileStorageService.getFileUrl(user.getProfileImagePath()), socialLinks);
    }

    @Override
    @Transactional
    public void createUser(UserRequest.Create request) {
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

        userRepository.save(user);
    }

    // ========== 프로필 이미지 처리 ========== //

    /**
     * 프로필 이미지 처리
     * 1. removeProfileImage == true → 기존 매핑 삭제 + profileImagePath = null
     * 2. 새 이미지 파일이 있으면 → 기존 매핑 삭제(고아 파일 전환) + 새 이미지 업로드/매핑 저장 + path 업데이트
     * 3. 둘 다 없으면 → 변경 없음
     */
    private void handleProfileImage(User user, UserRequest.UpdateProfile request, MultipartFile profileImage) {
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
