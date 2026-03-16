package com.sealog.backend.global.exception;

import com.sealog.backend.global.response.CustomResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * [전역 예외 처리기]
 * 어플리케이션 전역에서 발생하는 예외를 한곳에서 처리합니다.
 * 모든 에러 응답은 CustomResponse.error() 형식을 따릅니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리 (CustomException)
     */
    @ExceptionHandler(CustomException.class)
    public CustomResponse<Void> handleBusinessException(CustomException e, HttpServletResponse response) {
        log.error("Business Exception: {}", e.getMessage());
        response.setStatus(e.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return CustomResponse.error(e.getMessage(), e.getStatus().value());
    }

    /**
     * Bean Validation (@Valid) 예외 처리
     * 여러 에러 중 가장 첫 번째 에러 메시지 하나만 클라이언트에게 전달합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());

        // 첫 번째 에러 메시지 추출
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return CustomResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value());
    }

    /**
     * ConstraintViolationException 예외 처리 (주로 파라미터 레벨 검증 @CheckFile 등)
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse<Void> handleConstraintViolation(jakarta.validation.ConstraintViolationException e) {
        log.error("Constraint Violation: {}", e.getMessage());
        String errorMessage = e.getConstraintViolations().iterator().next().getMessage();
        return CustomResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value());
    }

    /**
     * HandlerMethodValidationException 예외 처리 (Spring Boot 3.2+ 신규 방식)
     */
    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse<Void> handleMethodValidationException(org.springframework.web.method.annotation.HandlerMethodValidationException e) {
        log.error("Method Validation Exception: {}", e.getMessage());
        String errorMessage = e.getAllErrors().get(0).getDefaultMessage();
        return CustomResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value());
    }

    /**
     * DB 제약 조건 위반 (중복 값 등) 예외 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public CustomResponse<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data Integrity Violation: {}", e.getMessage());

        return CustomResponse.error(
                "요청을 처리할 수 없습니다. 중복된 값이거나 유효하지 않은 요청입니다.",
                HttpStatus.CONFLICT.value()
        );
    }

    /**
     * 클라이언트가 연결을 끊었을 때 (영상 스트리밍 seek, 탭 이동 등 정상 동작)
     * 응답을 쓰지 않고 무시합니다.
     */
    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    @ResponseStatus(HttpStatus.OK)
    public void handleClientAbort(org.apache.catalina.connector.ClientAbortException e) {
        log.debug("Client disconnected: {}", e.getMessage());
    }

    /**
     * 그 외 예상치 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomResponse<Void> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        return CustomResponse.error(
                "서버 내부에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }
}
