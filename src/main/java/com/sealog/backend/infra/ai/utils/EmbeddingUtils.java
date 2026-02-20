package com.sealog.backend.infra.ai.utils;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Embedding 유틸 기능 제공 클래스
 * @author kcw
 * @since 2026-02-12
 */
@UtilityClass
public final class EmbeddingUtils {

    /**
     * Embedded 요청에 필요한 문자열 조합
     * @param sentences 임베딩 대상 객체 목록
     * @return 조합 문자열
     */
    public static String generateEmbeddingText(List<Object> sentences) {

        return sentences.stream()
                .filter(Objects::nonNull) // null 제거
                .map(EmbeddingUtils::convertObjToStr)
                .filter(str -> !str.isBlank())
                .collect(Collectors.joining(". ")) // 문장 간 간격은 ". " 기준으로 합침
                .trim() // 양끝 공백 제거
                + "."; // 문장 맨 마지막에 "." 추가
    }


    // Object -> String 변환 메소드
    private static String convertObjToStr(Object obj) {
        // Collection 값인 경우
        if (obj instanceof Collection<?> collection)
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .filter(str -> !str.isBlank())
                    .collect(Collectors.joining(", "));

        // 일반 객체인 경우
        return String.valueOf(obj).trim();
    }


    /**
     * embedded float 배열을 byte 문자열로 변환 (효율적 저장 및 조회 가능)
     * @param embedded 임베딩된 float 배열
     * @return byte 배열로 변환된 임베딩 배열
     */
    public static byte[] convertToByteArray(float[] embedded) {

        // [1] buffer 할당
        ByteBuffer buffer = ByteBuffer.allocate(embedded.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // [2] 변환 수행
        for (float value : embedded) buffer.putFloat(value);
        return buffer.array();
    }

}
