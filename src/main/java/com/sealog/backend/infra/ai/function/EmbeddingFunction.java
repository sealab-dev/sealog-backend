package com.sealog.backend.infra.ai.function;

import com.sealog.backend.infra.ai.constant.AiRedisKey;
import com.sealog.backend.infra.ai.utils.EmbeddingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * 단일 Embedding 요청 처리 함수
 */
@Component
@RequiredArgsConstructor
public class EmbeddingFunction implements Function<List<Object>, byte[]> {

    // embedding model
    private final EmbeddingModel embeddingModel;

    @Cacheable(
            value = AiRedisKey.CACHE_EMBEDDING,
            key = "@embeddingCacheKeyGenerator.generate(#sentences)"
    )
    @Override
    public byte[] apply(List<Object> sentences) {

        // [1] 임베딩 요청 객체 -> String 변환
        String requestText = EmbeddingUtils.generateEmbeddingText(sentences);

        // [2] 임베딩 수행
        float[] embedded = embeddingModel.embed(requestText);

        // [3] 임베딩 결과 float 배열 -> byte 배열 변환 후 반환
        return EmbeddingUtils.convertToByteArray(embedded);
    }
}
