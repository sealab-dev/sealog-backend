package com.sealog.backend.infra.storage.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FileStorageException extends RuntimeException {

    private final HttpStatus status;

    public FileStorageException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static FileStorageException notFound(String message) {
        return new FileStorageException(message, HttpStatus.NOT_FOUND);
    }

    public static FileStorageException badRequest(String message) {
        return new FileStorageException(message, HttpStatus.BAD_REQUEST);
    }

    public static FileStorageException unauthorized(String message) {
        return new FileStorageException(message, HttpStatus.UNAUTHORIZED);
    }

    public static FileStorageException forbidden(String message) {
        return new FileStorageException(message, HttpStatus.FORBIDDEN);
    }

    public static FileStorageException conflict(String message) {
        return new FileStorageException(message, HttpStatus.CONFLICT);
    }
}
