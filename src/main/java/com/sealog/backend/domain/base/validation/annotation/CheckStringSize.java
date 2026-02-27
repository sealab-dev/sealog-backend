package com.sealog.backend.domain.base.validation.annotation;

import com.sealog.backend.domain.base.validation.validator.CheckStringSizeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 문자열 검증을 위한 어노테이션
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckStringSizeValidator.class)
public @interface CheckStringSize {
    int min() default 1;                 // 최소 크기
    int max() default 255;               // 최대 크기
    boolean nullable() default false;    // null 허용 여부
    String message() default "";         // 메세지 미사용 (내부에서 자체적으로 생성)
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
