package com.sealog.backend.global.exception;

import com.sealog.backend.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Custom 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());

        ErrorResponse response;

        // 필드 에러가 있는 경우 errors 포함
        if (e.hasFieldErrors()) {
            response = ErrorResponse.of(
                    e.getMessage(),
                    e.getStatus().value(),
                    e.getErrors()
            );
        } else {
            response = ErrorResponse.of(
                    e.getMessage(),
                    e.getStatus().value()
            );
        }

        return ResponseEntity.status(e.getStatus()).body(response);
    }

    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("ValidationException: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = ErrorResponse.of(
                "입력값이 올바르지 않습니다",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // DB 제약 조건 위반 (동시 요청으로 인한 slug/title 충돌 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.of(
                "요청을 처리할 수 없습니다. 다시 시도해주세요",
                HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 그 외 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        ErrorResponse response = ErrorResponse.of(
                "서버 오류가 발생했습니다",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.internalServerError().body(response);
    }
}