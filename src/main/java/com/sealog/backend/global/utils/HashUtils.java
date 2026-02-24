package com.sealog.backend.global.utils;

import com.sealog.backend.global.exception.CustomException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Hash 생성 지원 클래스
 */
@UtilityClass
public class HashUtils {

    // 사용 상수
    public static final String SHA_256 = "SHA-256";
    public static final String MD5 = "MD5";

    /**
     * SHA-256 해시 생성 (전체)
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * String hash = HashUtils.sha256("Spring AI");
     * // → "a3f2c9d8e5f6b7a1c4d8e9f0a2b3c5d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3"
     * }</pre>
     *
     * @param text 해시할 텍스트
     * @return 64자 16진수 해시 문자열
     * @throws CustomException SHA-256 암호화 실패 시 (예외 발생 시)
     */
    public static String toSha256(String text) {
        return generateHash(text, SHA_256);
    }

    /**
     * SHA-256 해시 생성 (짧게)
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * String hash = HashUtils.sha256Short("Spring AI", 12);
     * // → "a3f2c9d8e5f6"
     * }</pre>
     *
     * @param text 해시할 텍스트
     * @param length 반환할 해시 길이 (최대 64)
     * @return 지정된 길이의 16진수 해시 문자열
     * @throws CustomException length가 0보다 작거나 64보다 큰 경우
     */
    public static String toSha256(String text, int length) {

        // 길이 검증
        if (length <= 0 || length > 64)
            throw new CustomException("SHA256 암호화 문자열 길이는 1~64 사이여야만 합니다. 입력 길이 : %d".formatted(length), HttpStatus.INTERNAL_SERVER_ERROR);

        // 암호화 수행 후, 앞에서 부터 길이 자름
        return toSha256(text).substring(0, length);
    }


    /**
     * MD5 해시 생성 (캐싱 용도로만 이용)
     *
     * @param text 해시할 텍스트
     * @return 32자 16진수 해시 문자열
     */
    public static String toMd5(String text) {
        return generateHash(text, MD5);
    }


    /**
     * MD5 해시 생성
     * @param text 암호화 대상 문자열
     * @param length 문자열 길이 (1~32 사이)
     */
    public static String toMd5(String text, int length) {

        // 길이 검증
        if (length <= 0 || length > 32)
            throw new CustomException("MD5 암호화 문자열 길이는 1~32 사이여야만 합니다. 입력 길이 : %d".formatted(length), HttpStatus.INTERNAL_SERVER_ERROR);

        // 암호화 수행 후, 앞에서 부터 길이 자름
        return toMd5(text).substring(0, length);
    }


    // 해시 생성
    private static String generateHash(String text, String hash) {

        try {
            MessageDigest digest = MessageDigest.getInstance(hash);
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new CustomException("%s 암호화를 할 수 없습니다! 원인 : %s".formatted(hash, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * byte 배열을 16진수 문자열로 변환
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * byte[] bytes = {0x0A, 0x1B, 0x2C};
     * String hex = HashUtils.bytesToHex(bytes);
     * // → "0a1b2c"
     * }</pre>
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
