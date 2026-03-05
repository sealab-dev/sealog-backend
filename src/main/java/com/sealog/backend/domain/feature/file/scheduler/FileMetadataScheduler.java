package com.sealog.backend.domain.feature.file.scheduler;

import com.sealog.backend.domain.feature.file.service.FileMetadataSchedulerService;
import com.sealog.backend.global.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 정리 스케줄러 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileMetadataScheduler {

    private final FileMetadataSchedulerService fileMetadataSchedulerService;

    /**
     * 매일 새벽 3시에 사용되지 않는 파일을 정리합니다.
     *
     * 트랜잭션 전략:
     * - S3 삭제 성공한 파일만 DB에서 삭제
     * - 일부 실패 시에도 성공한 것들은 정리 완료
     *
     * 동작 방식:
     * 1. 전체 수집: 모든 FileUsageCollector 구현체로부터 사용 중인 파일 ID 수집
     * 2. 합집합 생성: 수집한 ID들을 하나의 Set으로 통합
     * 3. 차집합 조회: FileMetadata에서 합집합에 포함되지 않은 파일 조회
     * 4. S3 멀티 삭제: 조회된 파일들을 한 번의 요청으로 삭제 (1000개씩 분할)
     * 5. DB 정리: S3 삭제 성공한 파일만 DB에서 삭제
     *
     * 확장성:
     * - 새로운 중간 테이블 추가 시 해당 서비스가 FileUsageCollector만 구현하면 자동 반영
     * - 예: UserFileService, CommentFileService 등
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void removeOrphans() {
        LogUtils.runAndShowCostLog("파일 정리 스케줄러", fileMetadataSchedulerService::removeOrphans);
    }

}