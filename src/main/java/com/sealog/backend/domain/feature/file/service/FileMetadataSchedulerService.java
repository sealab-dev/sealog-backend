package com.sealog.backend.domain.feature.file.service;


/**
 * 파일 메타데이터 스케줄러 인터페이스
 */
public interface FileMetadataSchedulerService {

    /**
     * 고아 상태가 된 파일 삭제
     */
    void removeOrphans();

}