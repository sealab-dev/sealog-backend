package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.file.support.FileUsageCollector;
import com.sealog.backend.domain.feature.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * UserFile 중간 테이블 관리 서비스
 *
 * 역할:
 * - User와 FileMetadata 간의 매핑 관리
 * - 프로필 이미지 업로드/연결/삭제 처리
 * - 사용자가 실제 사용하는 파일 조회
 */
public interface UserFileService extends FileUsageCollector {

    /**
     * 프로필 이미지를 업로드하고 매핑을 저장합니다.
     * S3 업로드 성공 후 DB 저장 실패 시 S3 파일을 삭제해 정합성을 보장합니다.
     *
     * @param user         업로드 요청 사용자
     * @param profileImage 업로드할 프로필 이미지 파일
     * @return 저장된 파일 경로 (User.profileImagePath 업데이트용)
     */
    String uploadAndSaveProfile(User user, MultipartFile profileImage);

    /**
     * 사용자의 기존 프로필 이미지 매핑 삭제 (고아 파일로 전환)
     *
     * @param userId 사용자 ID
     */
    void deleteProfile(Long userId);

    /**
     * 사용자의 프로필 이미지 파일 ID 조회
     *
     * @param userId 사용자 ID
     * @return 프로필 파일 ID (없으면 null)
     */
    Long getProfileFileId(Long userId);
}