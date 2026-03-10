package com.sealog.backend.domain.feature.file.service;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.global.exception.CustomException;

import java.util.List;
import java.util.Set;

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
     * 고아 파일(매핑 테이블에 존재하지 않고 생성 후 일정 시간 경과)을 조회합니다.
     *
     * @param hoursThreshold 기준 시간 (예: 24시간)
     * @return 고아 파일 목록
     */
    List<FileMetadata> findOrphanFiles(int hoursThreshold);

    /**
     * 존재하지 않거나 요청자 소유가 아닌 파일 ID를 반환합니다.
     * 단일 쿼리로 존재 여부와 소유권을 함께 확인합니다.
     *
     * @param fileIds 검증할 파일 ID 목록
     * @param userId  요청자 유저 ID
     * @return 유효하지 않은 파일 ID 집합 (존재하지 않거나 소유자 불일치)
     */
    Set<Long> findInvalidFileIds(List<Long> fileIds, Long userId);

}