package com.sealog.backend.domain.feature.file.service;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.global.exception.CustomException;

import java.util.List;

/**
 * 파일 메타데이터 관리 서비스
 *
 * 역할:
 * - 순수 메타데이터 CRUD 작업만 수행
 * - S3 업로드/삭제는 비즈니스 로직에서 직접 호출
 * - 트랜잭션은 비즈니스 로직 레이어에서 관리
 */
public interface FileMetadataService {

    /**
     * FileUploadResult를 기반으로 FileMetadata를 생성하고 저장합니다.
     *
     * @param uploadResult S3 업로드 결과
     * @param user 업로드 한 유저
     * @return 저장된 FileMetadata
     */
    FileMetadata upload(FileUploadResult uploadResult, User user);

    /**
     * 파일 메타데이터를 ID로 조회합니다.
     *
     * @param fileId 파일 ID
     * @return 파일 메타데이터
     */
    FileMetadata getMetadata(Long fileId);

    /**
     * 여러 파일 메타데이터를 ID 목록으로 조회합니다.
     *
     * @param fileIds 파일 ID 목록
     * @return 파일 메타데이터 목록
     */
    List<FileMetadata> getMetadataList(List<Long> fileIds);

    /**
     * 파일 메타데이터를 삭제합니다.
     *
     * @param fileId 파일 ID
     */
    void remove(Long fileId);

    /**
     * 여러 파일 ID가 모두 존재하는지 검증합니다.
     *
     * @param fileIds 검증할 파일 ID 목록
     * @throws CustomException 일부 파일이 존재하지 않을 경우
     */
    void validateFilesExist(List<Long> fileIds);

    /**
     * 고아 파일(매핑 테이블에 존재하지 않고 생성 후 일정 시간 경과)을 조회합니다.
     *
     * @param hoursThreshold 기준 시간 (예: 24시간)
     * @return 고아 파일 목록
     */
    List<FileMetadata> findOrphanFiles(int hoursThreshold);

    /**
     * 파일 목록이 특정 유저가 업로드한 파일인지 검증합니다.
     *
     * @param fileIds 검증할 파일 ID 목록
     * @param userId  요청자 유저 ID
     * @throws CustomException 소유자가 아닌 파일이 포함된 경우
     */
    void validateFilesOwnership(List<Long> fileIds, Long userId);

}