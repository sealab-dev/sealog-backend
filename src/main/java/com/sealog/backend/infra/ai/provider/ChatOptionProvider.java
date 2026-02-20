package com.sealog.backend.infra.ai.provider;

import com.sealog.backend.infra.ai.enums.ChatOption;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * AI 모델 옵셜 값 제공 인터페이스
 */
public interface ChatOptionProvider {

    /**
     * 채팅 옵션 값 제공
     * @param option 채팅 옵션 상수
     * @return ChatOptions
     */
    ChatOptions getOption(ChatOption option);
}
