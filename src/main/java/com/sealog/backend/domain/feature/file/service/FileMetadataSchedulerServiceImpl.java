package com.sealog.backend.domain.feature.file.service;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.file.repository.FileMetadataRepository;
import com.sealog.backend.domain.feature.file.support.FileUsageCollector;
import com.sealog.backend.infra.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMetadataSchedulerServiceImpl implements FileMetadataSchedulerService {

    // 사용 의존성
    private final FileStorageService fileStorageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final List<FileUsageCollector> fileUsageCollectors;

    // 사용 상수
    private static final int THRESHOLD_HOURS = 24;
    private static final int BATCH_SIZE = 1000;

    @Transactional
    @Override
    public void removeOrphans() {

        try {
            // 1. 모든 도메인에서 사용 중인 파일 ID 수집
            Set<Long> usedFileIds = collectAllUsedFileIds();

            if (usedFileIds.isEmpty()) {
                log.warn("=== 사용 중인 파일 ID가 하나도 없음 - 안전을 위해 삭제 스킵 ===");
                return;
            }

            log.debug("사용 중인 파일 ID 수집 완료: count={}", usedFileIds.size());

            // 2. 사용되지 않는 파일 조회 (24시간 경과 + 사용 중이 아닌 파일)
            List<FileMetadata> unusedFiles = getUnusedFiles(usedFileIds);

            if (unusedFiles.isEmpty()) {
                log.info("=== 삭제할 파일이 없습니다 ===");
                return;
            }

            log.debug("삭제 대상 파일 발견: count={}", unusedFiles.size());

            // 3. 엔티티 삭제
            Set<Long> unusedFileIds = unusedFiles.stream().map(FileMetadata::getId).collect(Collectors.toSet());
            fileMetadataRepository.deleteAllByIdInBatch(unusedFileIds);
            log.debug("DB 메타데이터 삭제 완료: count={}", unusedFileIds.size());

            // 4. 물리적 파일 삭제
            List<String> s3Keys = unusedFiles.stream().map(FileMetadata::getPath).toList();
            List<String> deletedS3Keys = fileStorageService.deleteFiles(s3Keys);

            if (deletedS3Keys.isEmpty()) {
                log.warn("=== S3 파일 삭제가 모두 실패했습니다 ===");
                return;
            }

            // 5. 결과 로그 출력
            log.info("=== 파일 정리 완료: 정리한 파일 개수 = {} ===", deletedS3Keys.size());

        } catch (Exception e) {
            log.error("파일 정리 스케줄러 실행 실패: {}", e.getMessage(), e);
        }
    }


    /**
     * 모든 FileUsageCollector로부터 사용 중인 파일 ID를 수집하여 합집합 생성
     *
     * 현재 구현된 컬렉터:
     * - PostFileService: 게시글에서 사용 중인 파일
     * - UserFileService: 사용자 프로필 등에서 사용 중인 파일
     *
     * 향후 추가 예정
     * - CommentFileService: 댓글에서 사용 중인 파일
     *
     * @return 사용 중인 파일 ID 합집합
     */
    private Set<Long> collectAllUsedFileIds() {

        return fileUsageCollectors.stream()
                .map(this::getFileIds)
                .filter(ids -> !ids.isEmpty())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * 파일 컬렉터로 부터 아이디 조회
     */
    private Set<Long> getFileIds(FileUsageCollector collector) {
        try {
            Set<Long> collectorFileIds = collector.collectUsedFileIds();
            log.debug("파일 ID 수집: collector={}, count={}", collector.getClass().getSimpleName(), collectorFileIds.size());
            return collectorFileIds;
        } catch (Exception e) {
            log.error("파일 ID 수집 실패: collector={}, error={}", collector.getClass().getSimpleName(), e.getMessage(), e);
            return Set.of();
        }
    }


    /**
     * 사용되지 않는 파일을 조회합니다 (합집합-차집합 전략)
     *
     * 동작 방식:
     * 1. 모든 도메인에서 수집한 "사용 중인 파일 ID" 합집합을 받음
     * 2. 전체 파일 중 합집합에 포함되지 않은 파일 조회
     */
    private List<FileMetadata> getUnusedFiles(Set<Long> usedFileIds) {

        // 현재 시간에서, threshold 만큼 뺌
        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(THRESHOLD_HOURS);

        // IN 절 제한 고려: 1000개씩 분할 처리
        List<FileMetadata> unusedFilesInBatches = findUnusedFilesInBatches(usedFileIds, thresholdTime);
        log.debug("사용되지 않는 파일 조회 완료: 기준={}시간 전, 개수={}", THRESHOLD_HOURS, unusedFilesInBatches.size());
        return unusedFilesInBatches;
    }


    /**
     * 사용 중인 파일 ID가 1000개 초과 시 분할 처리
     */
    private List<FileMetadata> findUnusedFilesInBatches(
            Set<Long> usedFileIds,
            LocalDateTime thresholdTime
    ) {
        List<Long> usedFileIdList = List.copyOf(usedFileIds);
        List<FileMetadata> allUnusedFiles = new ArrayList<>();

        for (int i = 0; i < usedFileIdList.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, usedFileIdList.size());
            Set<Long> batch = Set.copyOf(usedFileIdList.subList(i, endIndex));

            List<FileMetadata> batchUnusedFiles = fileMetadataRepository.findUnusedFiles(batch, thresholdTime);
            allUnusedFiles.addAll(batchUnusedFiles);

            log.debug("분할 조회 진행 중: batch={}/{}, 현재까지 누적={}",
                    (i / BATCH_SIZE) + 1,
                    (usedFileIdList.size() + BATCH_SIZE - 1) / BATCH_SIZE,
                    allUnusedFiles.size()
            );
        }

        log.debug("분할 조회 완료: 기준={}시간 전, 전체 개수={}", THRESHOLD_HOURS, allUnusedFiles.size());
        return allUnusedFiles;
    }

}
