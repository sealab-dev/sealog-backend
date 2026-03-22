package com.sealog.backend.domain.feature.file.service;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.global.exception.CustomException;

import java.util.List;
import java.util.Set;

/**
 * 파일 메타데이터 도메인 비즈니스 규약 인터페이스
 */
public interface FileMetadataService {

    /**
     * 업로드 결과를 기반으로 파일 메타데이터를 저장합니다.
     *
     * @param uploadResult S3/로컬 업로드 결과
     * @param user         업로드 수행 사용자
     * @return 저장된 파일 메타데이터 엔티티
     */
    FileMetadata upload(FileUploadResult uploadResult, User user);

    /**
     * 파일 ID로 메타데이터를 조회합니다.
     *
     * @param fileId 조회할 파일 ID
     * @return 파일 메타데이터 엔티티
     * @throws CustomException.notFound 파일을 찾을 수 없는 경우 발생
     */
    FileMetadata getMetadata(Long fileId);

    /**
     * 여러 파일 ID 목록으로 메타데이터 목록을 조회합니다.
     *
     * @param fileIds 조회할 파일 ID 목록
     * @return 파일 메타데이터 엔티티 목록
     */
    List<FileMetadata> getMetadataList(List<Long> fileIds);

    /**
     * 파일 메타데이터를 삭제합니다.
     *
     * @param fileId 삭제할 파일 ID
     * @throws CustomException.notFound 삭제하려는 파일이 존재하지 않는 경우 발생
     */
    void remove(Long fileId);

    /**
     * 고아 파일(참조되지 않은 파일) 목록을 조회합니다.
     *
     * @param hoursThreshold 기준 시간 (해당 시간 이전에 생성된 파일만 조회)
     * @return 삭제 대상 고아 파일 메타데이터 목록
     */
    List<FileMetadata> findOrphanFiles(int hoursThreshold);

    /**
     * 유효하지 않은 파일 ID(존재하지 않거나 소유자가 다른 경우)를 식별합니다.
     *
     * @param fileIds 검증 대상 파일 ID 목록
     * @param userId  요청 사용자 ID
     * @return 유효하지 않은 파일 ID 집합
     */
    Set<Long> findInvalidFileIds(List<Long> fileIds, Long userId);

}
