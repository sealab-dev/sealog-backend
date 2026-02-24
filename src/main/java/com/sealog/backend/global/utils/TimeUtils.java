package com.sealog.backend.global.utils;

import lombok.experimental.UtilityClass;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Objects;


/**
 * 시간 유틸기능 제공 클래스
 */
@UtilityClass
public final class TimeUtils {

    // 시간 포메팅
    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 2025-01-01
    public static final DateTimeFormatter FORMATTER_DATE_NO_HYPHEN = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm:ss"); // 00:00:00
    public static final DateTimeFormatter FORMATTER_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 문자열 상수 (애노테이션용)
    public static final String ZONE_KOREA = "Asia/Seoul";
    public static final String ZONE_UTC = "UTC";

    // ZoneId 상수 (런타임용)
    public static final ZoneId ZONE_ID_KOREA = ZoneId.of(ZONE_KOREA);
    public static final ZoneId ZONE_ID_UTC = ZoneId.of(ZONE_UTC);


    /**
     * 현재 사용하는 시간대의 LocalDate 제공 (현재 : KR)
     * @return 현재 시간대의 LocalDate
     */
    public static LocalDate getNowLocalDate() {
        return LocalDate.now(ZONE_ID_KOREA);
    }


    /**
     * 현재 사용하는 시간대의 LocalDateTime 제공 (현재 : KR)
     * @return 현재 시간대의 LocalDateTime
     */
    public static LocalDateTime getNowLocalDateTime() {
        return LocalDateTime.now(ZONE_ID_KOREA);
    }


    /**
     * 시간 포메팅 (2025-01-01)
     * @param time 문자열 변환 대상 시간 객체
     * @return 포메팅 처리된 문자열
     */
    public static String formatDate(TemporalAccessor time) {
        return FORMATTER_DATE.format(time);
    }


    /**
     * 포메팅된 현재 시간 문자열 제공 (2025-01-01)
     * @return 포메팅 처리된 문자열
     */
    public static String formatNowDate() {
        return FORMATTER_DATE.format(getNowLocalDate());
    }


    /**
     * 시간 포메팅 (20250101)
     * @param time 문자열 변환 대상 시간 객체
     * @return 포메팅 처리된 문자열
     */
    public static String formatDateNoHyphen(TemporalAccessor time) {
        return FORMATTER_DATE_NO_HYPHEN.format(time);
    }


    /**
     * 포메팅된 현재 시간 문자열 제공 (2025-01-01)
     * @return 포메팅 처리된 문자열
     */
    public static String formatNowDateNoHyphen() {
        return FORMATTER_DATE_NO_HYPHEN.format(getNowLocalDate());
    }


    /**
     * 시간 포메팅 (00:00:00)
     * @param time 문자열 변환 대상 시간 객체
     * @return 포메팅 처리된 문자열
     */
    public static String formatTime(TemporalAccessor time) {
        return FORMATTER_TIME.format(time);
    }


    /**
     * 시간 포메팅 (00:00:00)
     * @return 포메팅 처리된 문자열
     */
    public static String formatNowTime() {
        return FORMATTER_TIME.format(getNowLocalDateTime());
    }


    /**
     * 시간 포메팅 (2025-01-01 00:00:00)
     * @param time 문자열 변환 대상 시간 객체
     * @return 포메팅 처리된 문자열
     */
    public static String formatDateTime(TemporalAccessor time) {
        return FORMATTER_DATE_TIME.format(time);
    }


    /**
     * 포메팅된 현재 시간 문자열 제공 (2025-01-01 00:00:00)
     * @return 포메팅 처리된 문자열
     */
    public static String formatNowDateTime() {
        return FORMATTER_DATE_TIME.format(getNowLocalDateTime());
    }



    /**
     * 한국식 번호 문자열로 포메팅 (0,000)
     * @param value 문자열 변환 대상 숫자
     * @return 포메팅 처리된 문자열
     */
    public static String formatKoreaNumber(Number value) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(value);
    }


    /**
     * Duration 시간, 분, 초 포메팅
     * @param duration 대상 시간 객체
     * @return 포메팅 처리된 문자열
     */
    public static String formatDuration(Duration duration) {

        // [1] 시간 정보 추출
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        // [2] "시간" 정보 유무에 따라 포멧 반환 (0분이여도, 00:01 식으로 표시)
        return hours > 0 ?
                String.format("%d:%02d:%02d", hours, minutes, seconds) : // "시" 정보가 있는 경우 "1:10:23"
                String.format("%d:%02d", minutes, seconds); // "시" 정보가 없는 경우 "15:24"
    }

    /**
     * Duration 시간, 분, 초 포메팅
     * @param secondStr "초"로 저장된 문자열
     * @return 포메팅 처리된 문자열
     */
    public static String formatDuration(String secondStr) {

        // [1] 유효하지 않은 시간 값이면 0:00 반환
        if (Objects.isNull(secondStr) || Objects.equals(secondStr, "0")) return "0:00";

        // [2] 시간 정보 추출
        long seconds = Long.parseLong(secondStr);
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        // [2] "시간" 정보 유무에 따라 포멧 반환 (0분이여도, 00:01 식으로 표시)
        return hours > 0 ?
                String.format("%d:%02d:%02d", hours, minutes, secs) : // "시" 정보가 있는 경우 "1:10:23"
                String.format("%d:%02d", minutes, secs); // "시" 정보가 없는 경우 "15:24"
    }
}
