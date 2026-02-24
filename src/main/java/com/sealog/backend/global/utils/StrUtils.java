package com.sealog.backend.global.utils;

import com.sealog.backend.global.exception.CustomException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.*;

/**
 * 문자열 관련 조작 기능 제공 유틸 클래스
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StrUtils {

    // 내부 상수
    private static final Random random = new Random();


    /**
     * 랜덤 숫자 문자열 생성
     * @param length 생성할 문자열 길이
     * @return 랜덤 생성된 숫자 문자열 (ex. 000123)
     */
    public static String createRandomNumString(int length) {

        // [1] 숫자 format, 랜덤 수 범위 계산
        String format = "%0" + length + "d";
        int randomRange = (int) Math.pow(10, length); // 10^length

        // [2] 랜덤 수 생성 및 반환
        return format.formatted(random.nextInt(randomRange));
    }


    /**
     * 랜덤 숫자 문자열 생성 (prefix 포함)
     * @param length 생성할 문자열 길이
     * @param prefix 생성할 숫자 문자열 앞에 올 문자열
     * @return 랜덤 생성된 숫자 문자열 (ex. ID000123)
     */
    public static String createRandomNumString(int length, String prefix) {
        return prefix + createRandomNumString(length);
    }


    /**
     * UUID 문자열 생성
     * @return UUID 문자열 ('-' 삭제)
     */
    public static String createUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * UUID 문자열 생성
     * @param  length 생성할 UUID 길이
     * @return UUID 문자열 ('-' 삭제)
     */
    public static String createUUID(int length) {

        // 길이 검증
        if (length <= 0 || length > 32)
            throw new CustomException("UUID 문자열 길이는 1~32 사이여야만 합니다. 입력 길이 : %d".formatted(length), HttpStatus.INTERNAL_SERVER_ERROR);

        // UUID 생성 및 반환
        return createUUID().substring(0, length);
    }


    /**
     * 문자열 시작부터 일정 길이만큼 문자열 자름(split)
     * @param text 대상 문자열
     * @param splitLen 자를 문자열 위치 (해당 길이를 이후의 문자열을 자르고 '...' 으로 대체)
     * @return split 처리한 문자열 (ex. 문자열...)
     */
    public static String splitWithStart(String text, int splitLen) {

        // null 검증
        if (Objects.isNull(text)) return null;

        // 문자열 자르기 수행
        return text.length() > splitLen ? text.substring(0, splitLen) + "..." : text;
    }


    /**
     * 문자열 내 줄바꿈 제거 및 과도한 공백 줄임 (일반화)
     * @param text 대상 문자열
     * @return split 처리한 문자열 (ex. 문자열...)
     */
    public static String normalizeWhitespace(String text) {

        return Objects.isNull(text) ? null :
                text.replaceAll("[\\r\\n]+", " ")  // 모든 줄바꿈을 공백으로 변환
                .replaceAll("\\s+", " ") // 연속된 공백을 하나로 압축
                .trim();
    }


    /**
     * 문자열 내 placeholder 값 대체 (ex. ${INPUT} 값을 대체)
     * @param text 대상 문자열 (placeholder 포함)
     * @param placeholders Map<Placeholder, 대체값>
     * @return placeholder 값을 채운 문자열
     */
    public static String fillPlaceholder(String text, Map<String, String> placeholders) {

        // 결과 탬플릿
        String result = text;

        // 탬플릿 내 Placeholder($) 값 채움
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = Objects.isNull(entry.getValue()) ? "" : entry.getValue();
            result = result.replace(entry.getKey(), value);
        }

        // 결과 반환
        return result;
    }


    /**
     * 해당 문자열이 유효한지 검증 (null 이거나 공백이 아님)
     * @param text 대상 문자열
     * @return 검증 결과 (false - 유효하지 않음)
     */
    public static boolean isValid(String text) {
        return Objects.nonNull(text) && !text.isBlank();
    }
}