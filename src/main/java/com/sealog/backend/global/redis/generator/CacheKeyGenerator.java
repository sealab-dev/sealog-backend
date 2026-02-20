package com.sealog.backend.global.redis.generator;

import com.sealog.backend.global.core.utils.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;


/**
 * CacheKey 생성 클래스
 */
@Component
@RequiredArgsConstructor
public class CacheKeyGenerator {

    // 사용 상수
    private static final int LENGTH_CACHE_KEY = 12;
    private static final String DELIMITER = "|";
    private static final String DELIMITER_COLLECTION = ",";

    /**
     * 객체 정보 기반 Cache Key 생성
     *
     * <p><b>예시:</b>
     * <pre>{@code
     * // 사용 예시 1
     * @Cacheable(
     *     value = "post:search",
     *     key = "@cacheKeyGenerator.generateKey(#userId, #keyword)"
     * )
     *
     * // 사용 예시 2
     * @Cacheable(
     *     value = "post:detail",
     *     key = "@cacheKeyGenerator.generateKey(#userId, #postId, #tags)"
     * )
     * }</pre>
     *
     * @param params 캐시 키로 변환할 객체 배열
     * @return Cache Key 문자열
     */
    public String generateKey(Object... params) {
        String normalized = normalize(params);
        return HashUtils.toSha256(normalized, LENGTH_CACHE_KEY);
    }


    // 객체를 정규화된 문자열로 변환
    private String normalize(Object... params) {

        return Arrays.stream(params)
                .filter(Objects::nonNull)
                .map(this::normalizeSingle)  // ← 단일 객체 정규화
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(DELIMITER));  // ← 구분자 추가!
    }


    // 파리미터 정규화 수행
    private String normalizeSingle(Object param) {

        // Collection 정규화
        if (param instanceof Collection<?> collection)
            return normalizeCollection(collection);

        // Map 정규화
        if (param instanceof Map<?, ?> map)
            return normalizeMap(map);

        // Array 정규화
        if (param.getClass().isArray())
            return normalizeArray(param);

        // 기본 타입 및 문자열인 경우
        return String.valueOf(param);
    }


    // Collection 정규화
    private String normalizeCollection(Collection<?> collection) {
        return collection.stream()
                .map(this::normalizeSingle)
                .collect(Collectors.joining(DELIMITER_COLLECTION));
    }

    // Map 정규화
    private String normalizeMap(Map<?, ?> map) {

        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(String::valueOf))) // key 기분 정렬
                .map(entry -> "%s:%s".formatted(normalizeSingle(entry.getKey()), normalizeSingle(entry.getValue())))
                .collect(Collectors.joining(DELIMITER_COLLECTION));
    }

    // Array 정규화
    private String normalizeArray(Object array) {

        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(DELIMITER_COLLECTION);
            sb.append(normalize(Array.get(array, i)));
        }

        return sb.toString();
    }
}
