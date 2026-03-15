package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.entity.UserFile;
import com.sealog.backend.domain.feature.user.enums.UserFileType;
import com.sealog.backend.domain.feature.user.repository.UserFileRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.infra.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * UserFile 중간 테이블 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFileServiceImpl implements UserFileService {

    private final UserFileRepository userFileRepository;
    private final FileStorageService fileStorageService;
    private final FileMetadataService fileMetadataService;

    /**
     * 프로필 이미지 업로드 → file_metadata 저장 → UserFile 매핑 저장
     * S3 업로드 성공 후 DB 저장 실패 시 S3 파일 롤백
     */
    @Override
    @Transactional
    public String uploadAndSaveProfile(User user, MultipartFile profileImage) {

        FileUploadResult uploadResult;
        try {
            uploadResult = fileStorageService.uploadPublicImage(profileImage);
        } catch (IOException e) {
            throw CustomException.badRequest("프로필 이미지 업로드에 실패했습니다: " + e.getMessage());
        }

        try {
            FileMetadata fileMetadata = fileMetadataService.upload(uploadResult, user);
            UserFile profileMapping = UserFile.builder()
                    .userId(user.getId())
                    .fileId(fileMetadata.getId())
                    .fileType(UserFileType.PROFILE)
                    .build();
            userFileRepository.save(profileMapping);
            log.debug("프로필 이미지 업로드 완료: userId={}, path={}", user.getId(), uploadResult.path());
            return uploadResult.path();
        } catch (Exception e) {
            fileStorageService.deleteFile(uploadResult.path());
            log.error("프로필 이미지 DB 저장 실패로 S3 파일 롤백: path={}", uploadResult.path());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteProfile(Long userId) {
        Optional<UserFile> existingProfile = userFileRepository
                .findByUserIdAndFileType(userId, UserFileType.PROFILE);

        if (existingProfile.isPresent()) {
            userFileRepository.delete(existingProfile.get());
            log.debug("기존 프로필 이미지 매핑 삭제 완료: userId={}, fileId={}",
                    userId, existingProfile.get().getFileId());
        }
    }

    @Override
    public Long getProfileFileId(Long userId) {
        return userFileRepository
                .findByUserIdAndFileType(userId, UserFileType.PROFILE)
                .map(UserFile::getFileId)
                .orElse(null);
    }

    @Override
    public Set<Long> collectUsedFileIds() {
        List<Long> usedFileIds = userFileRepository.findAllUsedFileIds();
        Set<Long> uniqueFileIds = Set.copyOf(usedFileIds);
        log.debug("UserFile에서 사용 중인 파일 ID 수집 완료: count={}", uniqueFileIds.size());
        return uniqueFileIds;
    }
}
