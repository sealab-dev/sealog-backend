package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.file.support.FileUsageCollector;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자-파일 매핑 도메인 비즈니스 규약 인터페이스
 *
 * - 사용자의 프로필 이미지 업로드 및 매핑을 관리합니다.
 */
public interface UserFileService extends FileUsageCollector {

    /**
     * 사용자의 프로필 이미지를 업로드하고 매핑 정보를 저장합니다.
     * - 기존 프로필 매핑이 있다면 제거합니다.
     *
     * @param user         업로드 수행 사용자 엔티티
     * @param profileImage 업로드할 이미지 파일
     * @return 저장된 이미지의 스토리지 경로
     * @throws CustomException 업로드 실패 또는 DB 저장 실패 시 발생
     */
    String uploadAndSaveProfile(User user, MultipartFile profileImage);

    /**
     * 사용자의 프로필 이미지 매핑 정보를 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    void deleteProfile(Long userId);

    /**
     * 사용자가 현재 사용 중인 프로필 이미지의 파일 ID를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 파일 ID (존재하지 않을 경우 null)
     */
    Long getProfileFileId(Long userId);
}
