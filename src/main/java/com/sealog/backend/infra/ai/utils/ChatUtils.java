package com.sealog.backend.infra.ai.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Embedding 유틸 기능 제공 클래스
 * @author kcw
 * @since 2026-02-12
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatUtils {

    /**
     * AI 프롬포트 결과에 포함된 불필요한 텍스트 제거 (마크다운 블록 등)
     * @param text AI 생성결과 문자열
     * @return 불필요한 텍스트를 제거한 문자열
     */
    public static String removeMarkdown(String text) {

        // [1] 유효하지 않은 텍스트 > 빈 문자열
        if (Objects.isNull(text) || text.isBlank()) return "";

        // [2] 텍스트 내 마크다운 및 불필요 문자 제거
        return text
                // [1] Markdown 코드블록 제거 (```언어\n내용\n``` 형태)
                .replaceAll("```[a-z]*\\n?", "")
                .replaceAll("```", "")

                // [2] 단일 백틱 제거 (`내용` 형태)
                .replaceAll("^`|`$", "")

                // [3] JSON 따옴표 제거 (전체가 "내용" 형태)
                .replaceAll("^\"", "")
                .replaceAll("\"$", "")

                // [5] 앞뒤 공백 제거
                .trim();
    }
}
