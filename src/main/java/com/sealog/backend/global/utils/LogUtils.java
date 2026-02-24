package com.sealog.backend.global.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 로깅 메세지 출력을 위한 유틸 클래스
 */

@Slf4j
@UtilityClass
public final class LogUtils {

    /**
     * 메소드를 수행하고, 메소드 완료에 소요된 시간을 측정한 로그 출력
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * LogUtils.runAndShowCostLog("자동 갱신 작업", service::sync)
     * }</pre>
     *
     * @param baseMessage 출력할 기본 메세지 (이 메세지 뒤에 -시작, -완료, -실패가 붙음)
     * @param method 수행할 메소드
     */
    public static void runAndShowCostLog(String baseMessage, Runnable method) {

        // 작업 시작 시간 기록
        long startTime = getStartTimeAndShowStartLog(baseMessage);

        try {
            method.run();
            showCompleteCostLog(baseMessage, startTime);
        } catch (Exception e) {
            showErrorCostLog(baseMessage, e, startTime);
            throw e; // 로그 출력 후 예외 재전파
        }
    }


    /**
     * 메소드를 수행하고, 메소드 완료에 소요된 시간을 측정한 로그 출력
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * String result = LogUtils.runAndShowCostLog("자동 갱신 작업", service::sync)
     * }</pre>
     *
     * @param baseMessage 출력할 기본 메세지 (이 메세지 뒤에 -시작, -완료, -실패가 붙음)
     * @param method 수행할 메소드
     */
    public static <T> T runAndShowCostLog(String baseMessage, Supplier<T> method) {

        // 작업 시작 시간 기록
        long startTime = getStartTimeAndShowStartLog(baseMessage);

        try {
            T result = method.get();
            showCompleteCostLog(baseMessage, startTime);
            return result;

        } catch (Exception e) {
            showErrorCostLog(baseMessage, e, startTime);
            throw e; // 로그 출력 후 예외 재전파
        }
    }


    // 시작 시간 생성 및 시작 로그 출력
    private static long getStartTimeAndShowStartLog(String baseMessage) {

        // 시작 시점
        long startTime = System.currentTimeMillis();

        // 작업 시작/끝 로그 출력
        log.info("⭐ {} 시작", baseMessage);
        return startTime;
    }


    // 성공 로그 출력 (걸린 시간 계산)
    private static void showCompleteCostLog(String baseMessage, long startTime) {
        log.info("✅ {} 완료 - [소요 시간: ⌛ {}]", baseMessage, calculateCost(startTime));
    }


    // 실패 로그 출력 (걸린 시간 계산)
    private static void showErrorCostLog(String baseMessage, Exception e, long startTime) {
        log.error("❌ {} 실패 - [소요 시간: ⌛ {}]\n오류: {}", baseMessage, calculateCost(startTime), e.getMessage());
    }


    // 걸린 시간(비용, 초) 계산
    public static String calculateCost(long start) {
        double cost = (System.currentTimeMillis() - start) / 1000.0;
        return "%.4f초".formatted(cost);
    }

}

