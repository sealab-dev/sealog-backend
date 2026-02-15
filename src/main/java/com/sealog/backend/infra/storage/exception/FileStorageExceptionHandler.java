package com.sealog.backend.infra.storage.exception;

import com.sealog.backend.global.core.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
public class FileStorageExceptionHandler {

    /**
     * 파일 저장소 관련 비즈니스 예외 처리 (파일 형식 오류, 업로드 실패 등)
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException e) {
        log.error("[FILE_STORAGE_ERROR] 메시지: {}, 상태코드: {}", e.getMessage(), e.getStatus());

        ErrorResponse response = ErrorResponse.of(
                e.getMessage(),
                e.getStatus().value()
        );

        return ResponseEntity.status(e.getStatus()).body(response);
    }
}
