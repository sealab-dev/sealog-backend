package com.sealog.backend.domain.base.validation.validator;

import com.sealog.backend.domain.base.util.ValidationUtils;
import com.sealog.backend.domain.base.validation.annotation.CheckStringSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * {@code @CheckStringSize} 검증 클래스
 */

@Slf4j
public class CheckStringSizeValidator implements ConstraintValidator<CheckStringSize, String> {

    // 어노테이션 정보
    private int min;
    private int max;
    private boolean nullable;

    // 검증 오류 메세지 상수
    private static final String MESSAGE_EMPTY = "공백은 입력할 수 없습니다";
    private static final String MESSAGE_MAX = "%,d자 이내로 입력해 주세요";
    private static final String MESSAGE_INTERVAL = "%,d~%,d자 사이로 입력해 주세요";

    @Override
    public void initialize(CheckStringSize annotation) {
        min = annotation.min();
        max = annotation.max();
        nullable = annotation.nullable();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        // 1. null이 가능한 경우, 파일을 업로드 하지 않은 (null) 경우 통과
        if (nullable && (Objects.isNull(value))) return true;

        // 2. 유효 문자열 검증 (null 이거나 공백만 있는 문자열이 아닌지)
        if (!StringUtils.hasText(value)) {
            ValidationUtils.addViolation(context, MESSAGE_EMPTY);
            return false;
        }

        // 3. 최소 길이 검증
        int length = value.trim().length(); // 양끝 공백 제거
        if (min > length || max < length) {
            ValidationUtils.addViolation(context, min <= 1 ? MESSAGE_MAX.formatted(max) : MESSAGE_INTERVAL.formatted(min, max));
            return false;
        }

        // 검증 성공 시 true 반환
        return true;
    }


}
