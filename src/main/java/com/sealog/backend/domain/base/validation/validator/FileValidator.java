package com.sealog.backend.domain.base.validation.validator;

import com.sealog.backend.domain.base.util.ValidationUtils;
import com.sealog.backend.domain.base.validation.annotation.File;
import com.sealog.backend.domain.base.validation.constant.AllowedFileConstant;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@code @File} 검증 클래스
 */

@Slf4j
public class FileValidator implements ConstraintValidator<File, MultipartFile> {

    // 사용 상수
    private final Tika tika = new Tika();

    // 어노테이션 정보
    private AllowedFileType[] allowed;
    private double maxSizeMB;
    private boolean nullable;

    @Override
    public void initialize(File annotation) {
        allowed = annotation.allowed();
        maxSizeMB = annotation.maxSizeMB();
        nullable = annotation.nullable();
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {

        // 1. null이 가능한 경우, 파일을 업로드 하지 않은 (null) 경우 통과
        if (nullable && (Objects.isNull(value))) return true;

        // 2. 파일 존재 검증
        if (Objects.isNull(value) || value.isEmpty()) {
            ValidationUtils.addViolation(context, "파일을 선택해 주세요.");
            return false;
        }

        // 파일 크기 검증
        long maxBytes = (long) (maxSizeMB * 1024 * 1024);  // MB를 Bytes로 변환
        if (value.getSize() > maxBytes) {
            double curMBSize = value.getSize() / 1024.0 / 1024.0;
            String message = "파일 크기는 %.1fMB 이하여야 합니다. (현재: %.2fMB)".formatted(maxSizeMB, curMBSize);
            ValidationUtils.addViolation(context, message);
            return false;
        }


        // 파일명 검증 (파일명이 없는 경우)
        String originalFilename = value.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            ValidationUtils.addViolation(context, "올바른 파일명을 가진 파일을 업로드 해 주세요.");
            return false;
        }

        // 확장자 검증 (csv 처럼 tika로 온전히 검증이 불가능한 경우 대비)
        String ext = getFileExt(value.getOriginalFilename());
        Set<String> allowedExts = getExts();
        if (!allowedExts.contains(ext)) {
            ValidationUtils.addViolation(context, "허용되지 않는 확장자입니다.");
            return false;
        }

        // MEME 검증
        try {
            Set<String> allowedMimes = getMimeTypes();
            String detectedType = tika.detect(value.getInputStream(), value.getOriginalFilename());

            if (!allowedMimes.contains(detectedType)) {
                ValidationUtils.addViolation(context, "허용되지 않는 파일 타입 입니다.");
                return false;
            }

            // 체크 예외 래핑
        } catch (IOException e) {
            ValidationUtils.addViolation(context, "파일 타입 검증에 실패했습니다.");
            return false;
        }

        // 검증 성공 시 true 반환
        return true;
    }


    /**
     * 파일 확장자 추출
     */
    private String getFileExt(String filename) {

        // 만약 유효하지 않은 파일명이면 null
        if (Objects.isNull(filename)) return null;

        // 파일 확장자가 있는 인덱스
        int extIdx = filename.lastIndexOf('.');

        // 파일 확장자가 존재하는 경우에만, 추출 후 반환
        return extIdx == -1 || extIdx == filename.length() - 1 ?
                "" : filename.substring(extIdx + 1);
    }


    /**
     * MIME 타입 조회 (허용 범위 내)
     */
    private Set<String> getMimeTypes() {

        return Stream.of(allowed)
                .map(this::getMimeTypeFromConst)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * MIME 타입을 상수 내에서 조회
     */
    private Set<String> getMimeTypeFromConst(AllowedFileType fileType) {

        return switch (fileType) {
            case IMAGE      -> AllowedFileConstant.MIME_IMAGE;
            case VIDEO      -> AllowedFileConstant.MIME_VIDEO;
            case DOCUMENT   -> AllowedFileConstant.MIME_DOCUMENT;
            case AUDIO      -> AllowedFileConstant.MIME_AUDIO;
            case ARCHIVE    -> AllowedFileConstant.MIME_ARCHIVE;
            default         -> AllowedFileConstant.MIME_ALL;
        };
    }


    /**
     * 확장자 조회 (허용 범위 내)
     */
    private Set<String> getExts() {

        return Stream.of(allowed)
                .map(this::getExtFromConst)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * 확장자를 상수 내에서 조회
     */
    private Set<String> getExtFromConst(AllowedFileType fileType) {

        return switch (fileType) {
            case IMAGE      -> AllowedFileConstant.EXT_IMAGE;
            case VIDEO      -> AllowedFileConstant.EXT_VIDEO;
            case DOCUMENT   -> AllowedFileConstant.EXT_DOCUMENT;
            case AUDIO      -> AllowedFileConstant.EXT_AUDIO;
            case ARCHIVE    -> AllowedFileConstant.EXT_ARCHIVE;
            default         -> AllowedFileConstant.EXT_ALL;
        };
    }



}
