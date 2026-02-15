package com.sealog.backend.infra.storage.util;

import com.sealog.backend.infra.storage.constant.StoragePath;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class FileKeyGenerator {

    /**
     * 파일 키 생성
     * 형식: {basePath}/{yyyy/MM/dd}/{UUID}.{extension}
     * 예: public/images/2026/01/21/550e8400-e29b-41d4-a716-446655440000.jpg
     *
     * @param pathType 저장 경로 타입
     * @param originalFilename 원본 파일명
     * @return 생성된 파일 키
     */
    public String generateKey(StoragePath pathType, String originalFilename) {
        return resolveDirectoryPath(pathType) + "/" + toUUIDName(originalFilename);
    }

    /**
     * 날짜 기반 디렉토리 경로 생성
     * 형식: {basePath}/{yyyy/MM/dd}
     */
    private String resolveDirectoryPath(StoragePath pathType) {
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return pathType.getBasePath() + "/" + datePath;
    }

    /**
     * UUID 기반 파일명 생성
     * 형식: {UUID}.{extension}
     */
    private String toUUIDName(String originalFilename) {
        // 확장자 추출 (마지막 '.' 부터 끝까지)
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");

        if (lastDotIndex != -1 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex);
        }

        // UUID와 확장자 결합하여 반환
        return UUID.randomUUID().toString() + extension;
    }
}
