package com.sealog.backend.domain.feature.file.verifier;

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
public interface FileMetadataVerifier {

    /**
     * 현재 파일 ID가 존재하는지 검증합니다.
     *
     * @param fileId 검증할 파일 ID
     * @throws CustomException 파일이 존재하지 않을 경우
     */
    void verifyExist(Long fileId);

    /**
     * 여러 파일 ID가 모두 존재하는지 검증합니다.
     *
     * @param fileIds 검증할 파일 ID 목록
     * @throws CustomException 일부 파일이 존재하지 않을 경우
     */
    void verifyAllExist(List<Long> fileIds);

}