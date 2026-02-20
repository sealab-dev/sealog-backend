package com.sealog.backend.infra.ai.redis;

import com.sealog.backend.global.core.utils.HashUtils;
import com.sealog.backend.infra.ai.utils.EmbeddingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Redis CacheKey 생성 클래스
 */
@Component
@RequiredArgsConstructor
public class EmbeddingCacheKeyGenerator {

    // 사용 상수
    private static final int LENGTH_EMBEDDING_KEY = 12;

    /**
     * 임베딩 텍스트 생성 후 Cache Key 생성
     * <p><b>예시:</b>
     * <pre>{@code
     * // 사용 예시
     * @Cacheable(
     *     value = "embedding",
     *     key = "@embeddingCacheKeyGenerator.generateKey(#sentences)"
     * )
     * }</pre>
     * @param params 임베딩 문자열 리스트
     * @return 임베딩 전용 Cache Key 문자열
     */
    public String generate(List<Object> params) {
        String text = EmbeddingUtils.generateEmbeddingText(params);
        return HashUtils.toSha256(text, LENGTH_EMBEDDING_KEY);
    }
}
