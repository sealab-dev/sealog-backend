package com.sealog.backend.domain.feature.file.service;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.file.repository.FileMetadataRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 파일 메타데이터 관리 서비스 구현체
 *
 * 설계 원칙:
 * - 순수 메타데이터 CRUD만 담당
 * - S3 업로드/삭제는 호출하지 않음 (비즈니스 로직에서 처리)
 * - 트랜잭션은 상위 레이어에서 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileMetadataServiceImpl implements FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    @Transactional
    public FileMetadata upload(FileUploadResult uploadResult, User user) {
        FileMetadata fileMetadata = FileMetadata.builder()
                .user(user)
                .originalName(uploadResult.originalName())
                .path(uploadResult.path())
                .contentType(uploadResult.contentType())
                .size(uploadResult.fileSize())
                .build();

        FileMetadata saved = fileMetadataRepository.save(fileMetadata);
        log.info("파일 메타데이터 저장 완료: fileId={}, path={}", saved.getId(), saved.getPath());

        return saved;
    }

    @Override
    public FileMetadata getMetadata(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("파일 메타데이터를 찾을 수 없음: fileId={}", fileId);
                    return new CustomException("파일을 찾을 수 없습니다.", NOT_FOUND);
                });
    }

    @Override
    public List<FileMetadata> getMetadataList(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }

        return fileMetadataRepository.findByIdInWithUser(fileIds);
    }

    @Override
    @Transactional
    public void remove(Long fileId) {
        if (!fileMetadataRepository.existsById(fileId)) {
            log.warn("삭제할 파일 메타데이터가 존재하지 않음: fileId={}", fileId);
            throw new CustomException("파일을 찾을 수 없습니다.", NOT_FOUND);
        }

        fileMetadataRepository.deleteById(fileId);
        log.info("파일 메타데이터 삭제 완료: fileId={}", fileId);
    }

    @Override
    public void validateFilesExist(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        long existingCount = fileMetadataRepository.countByIdIn(fileIds);

        if (existingCount != fileIds.size()) {
            log.error("일부 파일을 찾을 수 없음: 요청={}, 존재={}", fileIds.size(), existingCount);
            throw new CustomException("일부 파일을 찾을 수 없습니다.", NOT_FOUND);
        }
    }

    @Override
    public List<FileMetadata> findOrphanFiles(int hoursThreshold) {
        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(hoursThreshold);
        List<FileMetadata> orphanFiles = fileMetadataRepository.findOrphanFiles(thresholdTime);

        log.info("고아 파일 조회 완료: 기준={}시간 전, 개수={}", hoursThreshold, orphanFiles.size());
        return orphanFiles;
    }

    @Override
    public void validateFilesOwnership(List<Long> fileIds, Long userId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        List<FileMetadata> files = fileMetadataRepository.findByIdInWithUser(fileIds);

        List<Long> unauthorizedIds = files.stream()
                .filter(f -> !f.getUser().getId().equals(userId))
                .map(FileMetadata::getId)
                .toList();

        if (!unauthorizedIds.isEmpty()) {
            log.error("파일 소유자 검증 실패: userId={}, unauthorizedFileIds={}", userId, unauthorizedIds);
            throw new CustomException("접근 권한이 없는 파일이 포함되어 있습니다.", FORBIDDEN);
        }

        log.debug("파일 소유자 검증 통과: userId={}, fileCount={}", userId, fileIds.size());
    }

}