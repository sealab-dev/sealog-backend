package com.sealog.backend.domain.base.validation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 검증을 위한 가능한 파일 타입 관리 클래스
 */
@Getter
@RequiredArgsConstructor
public enum AllowedFileType {
    IMAGE, VIDEO, DOCUMENT, AUDIO, ARCHIVE, ALL
}