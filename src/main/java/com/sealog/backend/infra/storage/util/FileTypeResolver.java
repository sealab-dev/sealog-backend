package com.sealog.backend.infra.storage.util;

import com.sealog.backend.global.file.constant.FileTypeConstants;
import com.sealog.backend.infra.storage.constant.StoragePath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileTypeResolver {

    /**
     * 파일 확장자 기반으로 저장 경로 결정
     *
     * @param file 업로드할 파일
     * @return 저장 경로
     */
    public static StoragePath resolveStoragePath(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());

        log.debug("파일 저장 경로 결정: {} (확장자: .{})", file.getOriginalFilename(), extension);

        // 확장자 기반 경로 분류
        if (FileTypeConstants.Image.ALLOWED_EXTENSIONS.contains(extension)) {
            return StoragePath.PUBLIC_IMAGE;
        }

        if (FileTypeConstants.Video.ALLOWED_EXTENSIONS.contains(extension)) {
            return StoragePath.PUBLIC_VIDEO;
        }

        if (FileTypeConstants.Document.ALLOWED_EXTENSIONS.contains(extension)) {
            return StoragePath.PUBLIC_DOCUMENT;
        }

        if (FileTypeConstants.Audio.ALLOWED_EXTENSIONS.contains(extension)) {
            return StoragePath.PUBLIC_AUDIO;
        }

        if (FileTypeConstants.Archive.ALLOWED_EXTENSIONS.contains(extension)) {
            return StoragePath.PUBLIC_ARCHIVE;
        }

        // FileValidator에서 이미 확장자 검증을 완료했으므로 여기 도달하지 않음
        log.warn("분류되지 않은 파일 확장자, 기본 경로 적용: .{}", extension);
        return StoragePath.PUBLIC_ASSET;
    }

    /**
     * 파일명에서 확장자 추출
     */
    public static String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
