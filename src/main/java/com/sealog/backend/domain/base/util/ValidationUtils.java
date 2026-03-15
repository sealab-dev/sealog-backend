package com.sealog.backend.domain.base.util;

import jakarta.validation.ConstraintValidatorContext;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.util.List;
import java.util.Objects;


/**
 * 검증기 유틸 클래스
 */

@UtilityClass
public class ValidationUtils {

    /**
     * Bean Validation 검증기에 커스텀 메세지 삽입 (기본 메세지 무효화)
     * @param context ConstraintValidatorContext
     * @param message 오류 메세지
     */
    public static void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
