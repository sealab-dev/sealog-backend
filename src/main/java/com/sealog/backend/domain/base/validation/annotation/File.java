package com.sealog.backend.domain.base.validation.annotation;

import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.base.validation.validator.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 이미지 파일 검증을 위한 어노테이션
 */

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface File {
    AllowedFileType[] allowed() default {AllowedFileType.ALL};  // 허용 MIME 타입
    double maxSizeMB() default 5.0;     // 기본 5MB
    boolean nullable() default false;   // null 불허
    String message() default "";        // 메세지 미사용 (내부에서 자체적으로 생성)
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
