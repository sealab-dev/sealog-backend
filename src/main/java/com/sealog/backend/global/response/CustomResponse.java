package com.sealog.backend.global.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomResponse<T> {
    
    private boolean success;
    private T data;
    private String message;
    
    public static <T> CustomResponse<T> success(T data) {
        return CustomResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    public static <T> CustomResponse<T> success(T data, String message) {
        return CustomResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    public static <T> CustomResponse<T> error(String message) {
        return CustomResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
