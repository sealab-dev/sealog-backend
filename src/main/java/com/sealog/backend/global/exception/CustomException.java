package com.sealog.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * [전역 비즈니스 예외 클래스]
 * 서비스 로직 중 발생하는 다양한 예외 상황을 처리합니다.
 * 에러의 의미를 직관적으로 알 수 있는 정적 팩토리 메서드를 제공합니다.
 */
@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // ========== 비즈니스 예외 전용 메서드 ========== //

    /**
     * 400 Bad Request: 클라이언트의 요청이 잘못되었을 때 사용
     * 예: 파라미터 유효성 검사 실패, 잘못된 로직 요청 등
     */
    public static CustomException badRequest(String message) {
        return new CustomException(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * 401 Unauthorized: 인증이 필요하거나 인증에 실패했을 때 사용
     * 예: 로그인 실패, 만료된 토큰 등
     */
    public static CustomException unauthorized(String message) {
        return new CustomException(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 403 Forbidden: 인증은 되었으나 해당 자원에 대한 권한이 없을 때 사용
     * 예: 다른 유저의 게시글 삭제 시도 등
     */
    public static CustomException forbidden(String message) {
        return new CustomException(message, HttpStatus.FORBIDDEN);
    }

    /**
     * 404 Not Found: 요청한 자원이 존재하지 않을 때 사용
     * 예: 존재하지 않는 게시글, 사용자 조회 등
     */
    public static CustomException notFound(String message) {
        return new CustomException(message, HttpStatus.NOT_FOUND);
    }

    /**
     * 409 Conflict: 서버의 현재 상태와 요청이 충돌할 때 사용
     * 예: 중복된 이메일, 중복된 닉네임 등
     */
    public static CustomException conflict(String message) {
        return new CustomException(message, HttpStatus.CONFLICT);
    }
}
