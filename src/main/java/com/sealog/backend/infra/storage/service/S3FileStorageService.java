package com.sealog.backend.infra.storage.service;

import com.sealog.backend.infra.storage.constant.StoragePath;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.infra.storage.exception.FileStorageException;
import com.sealog.backend.infra.storage.util.FileKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.InvalidationBatch;
import software.amazon.awssdk.services.cloudfront.model.Paths;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sealog.backend.infra.storage.util.FileTypeResolver.*;

/**
 * S3 파일 저장소 서비스 구현체 (운영 환경)
 *
 * 핵심 특징:
 * - FileMetadata 의존성 완전 제거 (독립적인 S3 비즈니스 로직)
 * - CloudFront 캐시 무효화 처리
 * - FileUploadResult DTO를 통한 데이터 반환
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "sealog.storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final CloudFrontClient cloudFrontClient;
    private final FileKeyGenerator fileKeyGenerator;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    @Value("${spring.cloud.aws.cloudfront.distribution-id:}")
    private String distributionId;

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
    public String getPresignedUrl(String fileKey, int minutes) {

        if (fileKey == null || fileKey.isBlank()) {
            log.warn("파일 키가 null 또는 비어있음");
            throw FileStorageException.badRequest("파일 키가 유효하지 않습니다.");
        }

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(minutes))
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presigned.url().toString();

            log.info("Presigned URL 생성 완료: path={}, 유효시간={}분", fileKey, minutes);
            return presignedUrl;

        } catch (S3Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.awsErrorDetails().errorMessage(), e);
            throw FileStorageException.badRequest("Presigned URL 생성 실패: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Presigned URL 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw FileStorageException.badRequest("Presigned URL 생성 실패: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            log.warn("파일 키가 null 또는 비어있음");
            return;
        }

        try {
            // 1. S3에서 파일 삭제
            deleteFromS3(fileKey);

            // 2. CloudFront 캐시 무효화
            invalidateCloudFrontCache(fileKey);

            log.info("파일 삭제 및 캐시 무효화 완료: path={}", fileKey);

        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: key={}, error={}", fileKey, e.awsErrorDetails().errorMessage(), e);
            throw FileStorageException.badRequest("S3 파일 삭제 실패: " + fileKey);
        } catch (Exception e) {
            log.error("파일 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw FileStorageException.badRequest("파일 삭제 실패: " + e.getMessage());
        }
    }

    @Override
    public List<String> deleteFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            log.warn("삭제할 파일 키 목록이 비어있음");
            return List.of();
        }

        // null 또는 빈 문자열 필터링
        List<String> validKeys = fileKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .collect(Collectors.toList());

        if (validKeys.isEmpty()) {
            log.warn("유효한 파일 키가 없음");
            return List.of();
        }

        log.info("멀티 파일 삭제 시작: 전체={}, 유효={}", fileKeys.size(), validKeys.size());

        List<String> allDeletedKeys = new ArrayList<>();

        try {
            // 1. S3 멀티 삭제 (1000개씩 분할)
            allDeletedKeys = deleteMultipleFromS3(validKeys);

            // 2. CloudFront 멀티 캐시 무효화
            if (!allDeletedKeys.isEmpty()) {
                invalidateMultipleCloudFrontCache(allDeletedKeys);
            }

            log.info("멀티 파일 삭제 완료: 요청={}, 성공={}", validKeys.size(), allDeletedKeys.size());
            return allDeletedKeys;

        } catch (Exception e) {
            log.error("멀티 파일 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            // 일부 성공한 경우도 있으므로 성공한 목록 반환
            return allDeletedKeys;
        }
    }

    @Override
    public String getFileUrl(String fileKey) {

        // 1. 주소 유효성 검증
        if (fileKey == null || fileKey.isBlank()) {
            log.warn("파일 키가 null 또는 비어있음");
            throw FileStorageException.badRequest("파일 키가 유효하지 않습니다.");
        }

        // 2. 로컬 환경에서는 직접 접근 URL 반환
        String url = generatePublicUrl(fileKey);
        log.info("S3 클라우드 파일 Public URL 생성 완료: path={}", url);
        return url;
    }

    @Override
    public String getBaseUrl() {
        return generatePublicBaseUrl();
    }

    /* ========== Private 메서드 ============ */

    /**
     * 파일 주소 유효성 검증
     */
    private void validateFileKey(String fileKey) {

        if (fileKey == null || fileKey.isBlank()) {
            log.warn("파일 키가 null 또는 비어있음");
            throw FileStorageException.badRequest("파일 키가 유효하지 않습니다.");
        }
    }

    /**
     * 파일을 S3에 업로드하고 메타데이터 반환
     */
    private FileUploadResult uploadFileInternal(
            MultipartFile file,
            StoragePath storagePath
    ) throws IOException {

        try {
            // 파일 키 생성 (경로 + UUID 파일명)
            String fileKey = fileKeyGenerator.generateKey(storagePath, file.getOriginalFilename());

            // S3 업로드 요청 생성
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .cacheControl("public, max-age=604800")
                    .build();

            // S3에 파일 업로드
            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("S3 파일 업로드 성공: bucket={}, key={}", bucketName, fileKey);

            // FileUploadResult 반환 (비즈니스 로직에서 DB 저장 처리)
            return FileUploadResult.builder()
                    .originalName(file.getOriginalFilename())
                    .path(fileKey)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (S3Exception e) {
            log.error("S3 업로드 실패: bucket={}, error={}", bucketName, e.awsErrorDetails().errorMessage(), e);
            throw FileStorageException.badRequest("S3 업로드 중 오류 발생: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            log.error("파일 처리 실패: {}", e.getMessage(), e);
            throw FileStorageException.badRequest("파일 읽기 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw FileStorageException.badRequest("예상치 못한 오류 발생: " + e.getMessage());
        }
    }

    /**
     * S3에서 파일 삭제
     */
    private void deleteFromS3(String fileKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("S3 파일 삭제 성공: bucket={}, key={}", bucketName, fileKey);
    }

    /**
     * S3에서 여러 파일 일괄 삭제 (1000개씩 분할 처리)
     *
     * @param fileKeys 삭제할 파일 키 목록
     * @return 삭제 성공한 파일 키 목록
     */
    private List<String> deleteMultipleFromS3(List<String> fileKeys) {
        List<String> allDeletedKeys = new ArrayList<>();
        int batchSize = 1000; // AWS S3 제한

        for (int i = 0; i < fileKeys.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, fileKeys.size());
            List<String> batch = fileKeys.subList(i, endIndex);

            try {
                // ObjectIdentifier 목록 생성
                List<ObjectIdentifier> objectIdentifiers = batch.stream()
                        .map(key -> ObjectIdentifier.builder().key(key).build())
                        .collect(Collectors.toList());

                // DeleteObjectsRequest 생성
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(builder -> builder.objects(objectIdentifiers).quiet(false))
                        .build();

                // S3 멀티 삭제 실행
                DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);

                // 성공한 파일 목록 수집
                List<String> deletedKeys = response.deleted().stream()
                        .map(DeletedObject::key)
                        .toList();

                allDeletedKeys.addAll(deletedKeys);

                // 실패한 파일 로깅
                if (response.hasErrors()) {
                    response.errors().forEach(error ->
                            log.error("S3 파일 삭제 실패: key={}, code={}, message={}",
                                    error.key(), error.code(), error.message())
                    );
                }

                log.info("S3 멀티 삭제 배치 완료: batch={}/{}, 성공={}, 실패={}",
                        (i / batchSize) + 1,
                        (fileKeys.size() + batchSize - 1) / batchSize,
                        deletedKeys.size(),
                        response.errors().size());

            } catch (S3Exception e) {
                log.error("S3 멀티 삭제 배치 실패: batch={}/{}, error={}",
                        (i / batchSize) + 1,
                        (fileKeys.size() + batchSize - 1) / batchSize,
                        e.awsErrorDetails().errorMessage(), e);
                // 배치 실패 시에도 계속 진행 (다음 배치 시도)
            }
        }

        return allDeletedKeys;
    }

    /**
     * CloudFront 캐시 무효화
     */
    private void invalidateCloudFrontCache(String fileKey) {
        if (distributionId == null || distributionId.isBlank()) {
            log.warn("CloudFront Distribution ID가 설정되지 않아 캐시 무효화를 건너뜁니다.");
            return;
        }

        try {
            String path = "/" + fileKey;

            Paths invalidationPaths = Paths.builder()
                    .items(path)
                    .quantity(1)
                    .build();

            InvalidationBatch batch = InvalidationBatch.builder()
                    .paths(invalidationPaths)
                    .callerReference(String.valueOf(System.currentTimeMillis()))
                    .build();

            CreateInvalidationRequest request = CreateInvalidationRequest.builder()
                    .distributionId(distributionId)
                    .invalidationBatch(batch)
                    .build();

            CreateInvalidationResponse response = cloudFrontClient.createInvalidation(request);
            log.info("CloudFront 캐시 무효화 성공: distributionId={}, invalidationId={}, path={}",
                    distributionId, response.invalidation().id(), path);

        } catch (Exception e) {
            log.error("CloudFront 캐시 무효화 실패: {}", e.getMessage(), e);
            // 캐시 무효화 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }


    /**
     * CloudFront 멀티 캐시 무효화
     *
     * CloudFront는 한 번의 요청으로 최대 3,000개의 경로를 무효화할 수 있습니다.
     * 와일드카드(*)를 사용하면 전체 무효화도 가능하지만, 명시적 경로 무효화가 더 안전합니다.
     *
     * @param fileKeys 캐시 무효화할 파일 키 목록
     */
    private void invalidateMultipleCloudFrontCache(List<String> fileKeys) {
        if (distributionId == null || distributionId.isBlank()) {
            log.warn("CloudFront Distribution ID가 설정되지 않아 캐시 무효화를 건너뜁니다.");
            return;
        }

        if (fileKeys == null || fileKeys.isEmpty()) {
            log.info("캐시 무효화할 경로가 없음");
            return;
        }

        try {
            // 파일 키를 CloudFront 경로로 변환 (/로 시작)
            List<String> paths = fileKeys.stream()
                    .map(key -> "/" + key)
                    .collect(Collectors.toList());

            // CloudFront는 한 번에 최대 3,000개까지 지원하지만, 안전하게 1,000개씩 분할
            int batchSize = 1000;
            int totalBatches = (paths.size() + batchSize - 1) / batchSize;

            for (int i = 0; i < paths.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, paths.size());
                List<String> batchPaths = paths.subList(i, endIndex);

                Paths invalidationPaths = Paths.builder()
                        .items(batchPaths)
                        .quantity(batchPaths.size())
                        .build();

                InvalidationBatch batch = InvalidationBatch.builder()
                        .paths(invalidationPaths)
                        .callerReference(String.valueOf(System.currentTimeMillis()) + "-" + i)
                        .build();

                CreateInvalidationRequest request = CreateInvalidationRequest.builder()
                        .distributionId(distributionId)
                        .invalidationBatch(batch)
                        .build();

                CreateInvalidationResponse response = cloudFrontClient.createInvalidation(request);

                log.info("CloudFront 멀티 캐시 무효화 완료: batch={}/{}, invalidationId={}, paths={}",
                        (i / batchSize) + 1, totalBatches,
                        response.invalidation().id(), batchPaths.size());
            }

        } catch (Exception e) {
            log.error("CloudFront 멀티 캐시 무효화 실패: {}", e.getMessage(), e);
            // 캐시 무효화 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }

    /**
     * 공개 URL 생성 (CloudFront 우선)
     */
    private String generatePublicUrl(String fileKey) {

        String baseUrl = generatePublicBaseUrl();

        return fileKey.startsWith("/")
                ? "%s%s".formatted(baseUrl, fileKey)
                : "%s/%s".formatted(baseUrl, fileKey);
    }

    /**
     * 공개 Base URL 생성 (CloudFront 우선)
     */
    private String generatePublicBaseUrl() {

        return cloudFrontDomain != null && !cloudFrontDomain.isBlank()
                ? "https://%s".formatted(cloudFrontDomain)
                : "https://%s.s3.%s.amazonaws.com".formatted(bucketName, region);
    }


    /**
     * 파일 키에서 저장된 파일명 추출
     */
    private String extractStoredName(String fileKey) {
        int lastSlashIndex = fileKey.lastIndexOf('/');
        return lastSlashIndex != -1 ? fileKey.substring(lastSlashIndex + 1) : fileKey;
    }
}
