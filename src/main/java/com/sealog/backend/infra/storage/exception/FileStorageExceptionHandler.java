package com.sealog.backend.infra.storage.exception;

import com.sealog.backend.global.response.CustomResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 파일 저장소 관련 전역 예외 핸들러
 */
@Slf4j
@RestControllerAdvice
public class FileStorageExceptionHandler {

    @ExceptionHandler(FileStorageException.class)
    public CustomResponse<Void> handleFileStorageException(FileStorageException e, HttpServletResponse response) {
        log.error("FileStorageException: {}", e.getMessage());
        response.setStatus(e.getStatus().value());

        return CustomResponse.error(
                e.getMessage(),
                e.getStatus().value()
        );
    }
}
