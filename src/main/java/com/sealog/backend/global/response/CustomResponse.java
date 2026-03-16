package com.sealog.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * [전역 공통 응답 규격]
 * 모든 API 응답은 이 봉투에 담겨서 나갑니다.
 */
@Getter
@Builder
@Schema(description = "공통 응답 봉투")
public class CustomResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "응답 데이터 (API마다 다름)")
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "응답 메시지 (액션 성공·실패 시 반환)", example = "게시글이 삭제되었습니다")
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "HTTP 상태 코드", example = "200")
    private final Integer status;

    // ========== 성공 응답 (상태 코드는 시스템이 자동 주입) ========== //

    /**
     * 1. 데이터만 반환 (가장 일반적인 경우)
     */
    public static <T> CustomResponse<T> success(T data) {
        return CustomResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 2. 메시지만 반환 (삭제, 수정 등 액션 성공 시)
     */
    public static <T> CustomResponse<T> success(String message) {
        return CustomResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * 3. 데이터와 메시지 모두 반환
     */
    public static <T> CustomResponse<T> success(T data, String message) {
        return CustomResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * [내부 시스템용] 상태 코드를 포함한 성공 응답 생성
     * 💡 ResponseWrapperAdvice에서 최종적으로 호출합니다.
     */
    public static <T> CustomResponse<T> success(T data, String message, int status) {
        return CustomResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .status(status)
                .build();
    }

    // ========== 에러 응답 ========== //

    public static <T> CustomResponse<T> error(String message, int status) {
        return CustomResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .build();
    }
}
