package com.sealog.backend.infra.ai.provider;

import com.sealog.backend.infra.ai.enums.ChatOption;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * OpenAI 기반 ChatOptionProvider 구현 클래스
 */
@Component
public class OpenAiChatOptionProvider implements ChatOptionProvider {

    // 옵션 상수
    private static final Map<ChatOption, OpenAiChatOptions> OPTIONS_MAP = Map.of(

            // 정확성 최우선 - 규칙 엄수, 일관된 결과
            ChatOption.STRICT, OpenAiChatOptions.builder()
                    .temperature(0.0)
                    .topP(0.8)
                    .build(),

            // 제한된 다양성 - 데이터 기반 해석
            ChatOption.MODERATE, OpenAiChatOptions.builder()
                    .temperature(0.75)
                    .topP(0.95)
                    .frequencyPenalty(0.2)
                    .build(),

            // 높은 다양성 - 다양한 관점/표현
            ChatOption.CREATIVE, OpenAiChatOptions.builder()
                    .temperature(1.0)       // 다양성을 가지는 응답 (1.0이 사실상 최대치)
                    .topP(0.95)             // 토큰 선택 방식 (높을수록 개방적)
                    .presencePenalty(0.6)   // 새로운 주제/관점 유도 (높을수록 다양한 관점 분석)
                    .frequencyPenalty(0.4)  // 반복 표현 억제 정도 (높을수록 반복표현 감소)
                    .build(),

            // 정확성 최우선 - 규칙 엄수, 일관된 결과
            ChatOption.AGENT, OpenAiChatOptions.builder()
                    .temperature(0.3)
                    .topP(0.9)
                    .build()
    );


    @Override
    public ChatOptions getOption(ChatOption option) {
        return OPTIONS_MAP.get(option);
    }
}
