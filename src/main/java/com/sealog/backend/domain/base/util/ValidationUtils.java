package com.sealog.backend.domain.base.util;

import jakarta.validation.ConstraintValidatorContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.util.List;
import java.util.Objects;


/**
 * 검증기 기능에 활용하기 위한 유틸 클래스
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtils {

    /**
     * 불필요한 HTML 태그 정화(제거)
     * @param text 원문 문자열
     * @return 정화된 HTML 문자열
     */
    public static String purifyHtml(String text) {

        return Objects.isNull(text) || text.isBlank() ?
                "" :
                Jsoup.clean(
                        text,
                        "https://example.com", // 상대경로 허용을 위한 더미 주소
                        Safelist.none(), // custom safelist
                        new Document.OutputSettings().prettyPrint(false) // 개행문자 허용
                );
    }

    /**
     * HTML 정화 후, 내부 이미지 src 요소 추출
     * @param text 원문 HTML 문자열
     * @return 정화 후 추출된 이미지 파일명 리스트
     */
    public static List<String> purifyAndExtractImageNameFromHtmlText(String text) {

        // [1] HTML 내 위험 코드 정화
        String purified = purifyHtml(text);
        Document purifiedDoc = Jsoup.parse(purified);

        // [2] 이미지 태그만 추출 후  src 속성만 추출 후 반환
        return purifiedDoc.select("img").stream()
                .map(img -> img.attr("src"))
                .filter(src -> !src.isBlank())
                .map(src -> src.substring(src.lastIndexOf('/') + 1))
                .toList();
    }

    /**
     * HTML 태그를 제거한 문자열만 추출
     * @param text 원문 문자열
     * @return 사용자가 작성한 "순수" 문자열 반환
     */
    public static String removeHtmlTags(String text) {
        return Objects.isNull(text) ? null : Jsoup.parse(text).text();
    }


    /**
     * 문자열이 SAFELIST 규칙을 준수하는지 검증
     * @param text 검증할 문자열
     * @return 허용되지 않은 태그/속성이 포함되면 false (통과 시 true)
     */
    public static boolean isValidText(String text) {
        return Objects.isNull(text) || text.isBlank() || Jsoup.isValid(text, Safelist.none());
    }


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
