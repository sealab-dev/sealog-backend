package com.sealog.backend.global.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
     * 작업을 수행하고, 작업 완료에 소요된 시간을 측정 및 반환
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * LogUtils.showAndCalculateCostLog("자동 갱신 작업", service::sync)
     * }</pre>
     *
     * @param baseMessage 출력할 기본 메세지 (이 메세지 뒤에 -시작, -완료, -실패가 붙음)
     * @param task 수행할 작업
     * @return 소요 시간 (ms)
     */
    public static long calculateAndShowCostLog(String baseMessage, Runnable task) {

        // 작업 시작 시간 기록
        long startTime = getStartTimeAndShowStartLog(baseMessage);

        try {
            task.run();
            return calculateAndShowCompleteCostLog(baseMessage, startTime);

        } catch (Exception e) {
            calculateAndShowErrorCostLog(baseMessage, e, startTime);
            throw e; // 로그 출력 후 예외 재전파
        }
    }


    /**
     * 작업을 수행하고, 작업 완료에 소요된 시간을 측정 및 결과 반환
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * LogUtils.showAndCalculateCostLog("자동 갱신 작업", service::sync)
     * }</pre>
     *
     * @param baseMessage 출력할 기본 메세지 (이 메세지 뒤에 -시작, -완료, -실패가 붙음)
     * @param task 수행할 작업
     * @return LogResponse(소요 시간 (ms), 작업 수행 결과) 반환
     */
    public static <T> LogResponse<T> calculateAndShowCostLog(String baseMessage, Supplier<T> task) {

        // 작업 시작 시간 기록
        long startTime = getStartTimeAndShowStartLog(baseMessage);

        try {
            T result = task.get();
            return new LogResponse<>(calculateAndShowCompleteCostLog(baseMessage, startTime), result);

        } catch (Exception e) {
            calculateAndShowErrorCostLog(baseMessage, e, startTime);
            throw e; // 로그 출력 후 예외 재전파
        }
    }



    /**
     * 작업을 수행하고, 작업 완료에 소요된 시간을 측정한 로그 출력
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * LogUtils.showCostLog("자동 갱신 작업", service::sync)
     * }</pre>
     *
     * @param baseMessage 출력할 기본 메세지 (이 메세지 뒤에 -시작, -완료, -실패가 붙음)
     * @param task 수행할 작업
     */
    public static void showCostLog(String baseMessage, Runnable task) {

        // 작업 시작 시간 기록
        long startTime = getStartTimeAndShowStartLog(baseMessage);

        try {
            task.run();
            calculateAndShowCompleteCostLog(baseMessage, startTime);

        } catch (Exception e) {
            calculateAndShowErrorCostLog(baseMessage, e, startTime);
            throw e; // 로그 출력 후 예외 재전파
        }
    }


    /**
     * 작업을 수행하고, 작업 완료에 소요된 시간을 측정한 로그 출력
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * String result = LogUtils.showCostLog("자동 갱신 작업", service::sync)
     * }</pre>
     *
     * @param baseMessage 출력할 기본 메세지 (이 메세지 뒤에 -시작, -완료, -실패가 붙음)
     * @param task 수행할 작업
     * @return 소요 시간 (ms)
     */
    public static <T> T showCostLog(String baseMessage, Supplier<T> task) {

        // 작업 시작 시간 기록
        long startTime = getStartTimeAndShowStartLog(baseMessage);

        try {
            T result = task.get();
            calculateAndShowCompleteCostLog(baseMessage, startTime);
            return result;

        } catch (Exception e) {
            calculateAndShowErrorCostLog(baseMessage, e, startTime);
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
    private static long calculateAndShowCompleteCostLog(String baseMessage, long startTime) {
        long cost = calculateCost(startTime);
        log.info("✅ {} 완료 - [소요 시간: ⌛ {} {}]",
                baseMessage,
                formatSecondCost(cost),
                formatMilliCost(cost)
        );
        return cost;
    }


    // 실패 로그 출력 (걸린 시간 계산)
    private static void calculateAndShowErrorCostLog(String baseMessage, Exception e, long startTime) {
        long cost = calculateCost(startTime);
        log.error("❌ {} 실패 - [소요 시간: ⌛ {} {}]\n오류: {}",
                baseMessage,
                formatSecondCost(cost),
                formatMilliCost(cost),
                e.getMessage()
        );
    }


    // 걸린 시간(비용, 초) 계산
    public static long calculateCost(long start) {
        return System.currentTimeMillis() - start;
    }

    // 걸린 시간(비용, 초) 계산
    public static String formatSecondCost(long cost) {
        return "%.3f초".formatted(cost / 1000.0);
    }

    // 걸린 시간(비용, 초) 계산
    public static String formatMilliCost(long cost) {
        return "(%,dms)".formatted(cost);
    }
    
    
    @Getter
    @AllArgsConstructor
    public static class LogResponse<T> {
        
        private long cost;
        private T result;
    }

}

