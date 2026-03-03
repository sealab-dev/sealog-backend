package com.sealog.backend.infra.storage.service;

import com.sealog.backend.infra.storage.constant.StoragePath;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.infra.storage.exception.FileStorageException;
import com.sealog.backend.infra.storage.properties.LocalStorageProperties;
import com.sealog.backend.infra.storage.util.FileKeyGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sealog.backend.infra.storage.util.FileTypeResolver.*;

/**
 * 로컬 파일 저장소 서비스 구현체 (로컬 개발 환경)
 *
 * 로컬 파일 시스템에 파일을 저장하고 관리합니다.
 * 기본 저장 경로: ./uploads
 */
@Slf4j
@Service
@Profile({"local", "test"})
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final LocalStorageProperties localStorageProperties;
    private final FileKeyGenerator fileKeyGenerator;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(localStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            log.info("로컬 업로드 디렉토리 생성 완료: {}", uploadPath);
        } catch (IOException e) {
            log.error("로컬 업로드 디렉토리 생성 실패: {}", e.getMessage(), e);
            throw FileStorageException.badRequest("업로드 디렉토리 생성 실패: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResult uploadPublicImage(MultipartFile file) throws IOException {
        return uploadFileInternal(file, StoragePath.PUBLIC_IMAGE);
    }

    @Override
    public FileUploadResult uploadPublicVideo(MultipartFile file) throws IOException {
        return uploadFileInternal(file, StoragePath.PUBLIC_VIDEO);
    }

    @Override
    public FileUploadResult uploadPublicDocument(MultipartFile file) throws IOException {
        return uploadFileInternal(file, StoragePath.PUBLIC_DOCUMENT);
    }

    @Override
    public FileUploadResult uploadPublicAudio(MultipartFile file) throws IOException {
        return uploadFileInternal(file, StoragePath.PUBLIC_AUDIO);
    }

    @Override
    public FileUploadResult uploadPublicArchive(MultipartFile file) throws IOException {
        return uploadFileInternal(file, StoragePath.PUBLIC_ARCHIVE);
    }

    @Override
    public FileUploadResult uploadPublicAssets(MultipartFile file) throws IOException {
        return uploadFileInternal(file, StoragePath.PUBLIC_ASSET);
    }

    @Override
    public FileUploadResult uploadFile(MultipartFile file) throws IOException {
        StoragePath storagePath = resolveStoragePath(file);
        return uploadFileInternal(file, storagePath);
    }

    @Override
    public void deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            log.warn("파일 키가 null 또는 비어있음");
            return;
        }

        try {
            Path filePath = resolveFilePath(fileKey);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("로컬 파일 삭제 성공: path={}", filePath);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: path={}", filePath);
            }
        } catch (IOException e) {
            log.error("로컬 파일 삭제 실패: key={}, error={}", fileKey, e.getMessage(), e);
            throw FileStorageException.badRequest("파일 삭제 실패: " + fileKey);
        }
    }

    @Override
    public List<String> deleteFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            log.warn("삭제할 파일 키 목록이 비어있음");
            return List.of();
        }

        List<String> validKeys = fileKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .collect(Collectors.toList());

        if (validKeys.isEmpty()) {
            log.warn("유효한 파일 키가 없음");
            return List.of();
        }

        log.info("멀티 파일 삭제 시작: 전체={}, 유효={}", fileKeys.size(), validKeys.size());

        List<String> deletedKeys = new ArrayList<>();

        for (String fileKey : validKeys) {
            try {
                Path filePath = resolveFilePath(fileKey);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    deletedKeys.add(fileKey);
                } else {
                    // 파일이 이미 없으면 삭제 성공으로 처리
                    deletedKeys.add(fileKey);
                    log.warn("삭제할 파일이 존재하지 않음 (성공 처리): path={}", filePath);
                }
            } catch (IOException e) {
                log.error("로컬 파일 삭제 실패: key={}, error={}", fileKey, e.getMessage(), e);
            }
        }

        log.info("멀티 파일 삭제 완료: 요청={}, 성공={}", validKeys.size(), deletedKeys.size());
        return deletedKeys;
    }

    @Override
    public String getPresignedUrl(String fileKey, int minutes) {
        if (fileKey == null || fileKey.isBlank()) {
            log.warn("파일 키가 null 또는 비어있음");
            throw FileStorageException.badRequest("파일 키가 유효하지 않습니다.");
        }

        // 로컬 환경에서는 직접 접근 URL 반환
        String baseUrl = localStorageProperties.getBaseUrl();
        String url = baseUrl + "/" + fileKey;
        log.info("로컬 파일 URL 생성 완료: path={}", url);
        return url;
    }

    /* ========== Private 메서드 ============ */

    /**
     * 파일을 로컬 파일 시스템에 저장하고 메타데이터 반환
     */
    private FileUploadResult uploadFileInternal(
            MultipartFile file,
            StoragePath storagePath
    ) throws IOException {

        try {
            // 파일 키 생성 (경로 + UUID 파일명)
            String fileKey = fileKeyGenerator.generateKey(storagePath, file.getOriginalFilename());

            // 로컬 저장 경로 생성
            Path filePath = resolveFilePath(fileKey);
            Files.createDirectories(filePath.getParent());

            // 파일 저장
            file.transferTo(filePath.toFile());
            log.info("로컬 파일 업로드 성공: path={}", filePath);

            return FileUploadResult.builder()
                    .originalName(file.getOriginalFilename())
                    .path(fileKey)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {
            log.error("로컬 파일 저장 실패: {}", e.getMessage(), e);
            throw FileStorageException.badRequest("파일 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 파일 키를 로컬 파일 시스템 경로로 변환
     */
    private Path resolveFilePath(String fileKey) {
        return Paths.get(localStorageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize()
                .resolve(fileKey);
    }
}
