package com.sealog.backend.infra.ai.function;

import com.sealog.backend.infra.ai.utils.EmbeddingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Embedding Batch 요청 처리 함수
 */
@Component
@RequiredArgsConstructor
public class BatchEmbeddingFunction<T> implements Function<Map<T, List<Object>>, Map<T, byte[]>> {

    // embedding model
    private final EmbeddingModel embeddingModel;

    @Override
    public Map<T, byte[]> apply(Map<T, List<Object>> idSentenceMap) {

        // [1] id(PK) 추출
        List<T> ids = new ArrayList<>(idSentenceMap.keySet());

        // [2] 임베딩 요청 문자열 생성
        List<String> sentences = ids.stream()
                .map(idSentenceMap::get)
                .map(EmbeddingUtils::generateEmbeddingText)
                .toList(); // ids 순서대로 리스트가 생성되도록 보장

        // [3] 임베딩 수행
        List<float[]> embedded = embeddingModel.embed(sentences);

        // [4] 임베딩 수행 결과 반환 (float[] -> byte[])
        return IntStream.range(0, ids.size())
                .boxed()
                .collect(Collectors.toMap(
                        ids::get,
                        i -> EmbeddingUtils.convertToByteArray(embedded.get(i))
                ));
    }
}
