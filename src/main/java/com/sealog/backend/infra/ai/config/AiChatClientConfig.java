package com.sealog.backend.infra.ai.config;

import com.sealog.backend.infra.ai.constant.AiChatPrompt;
import com.sealog.backend.infra.ai.enums.ChatOption;
import com.sealog.backend.infra.ai.provider.ChatOptionProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI ChatClient 설정 클래스
 */
@Configuration
@RequiredArgsConstructor
public class AiChatClientConfig {

    // 사용 의존성
    private final ChatOptionProvider chatOptionProvider;

    /**
     * 뉴스 요약에 사용하는 chat client
     */
    @Bean("newsChatClient")
    public ChatClient newsChatClient(ChatClient.Builder builder) {

        return builder
                .defaultSystem(AiChatPrompt.PROMPT_NEWS_DEFAULT)
                .defaultOptions(chatOptionProvider.getOption(ChatOption.CREATIVE))
                .build();
    }

    /**
     * AI agent chat client
     */
    @Bean("agentChatClient")
    public ChatClient agentChatClient(ChatClient.Builder builder) {

        return builder
                .defaultSystem(AiChatPrompt.PROMPT_AGENT_DEFAULT)
                .defaultOptions(chatOptionProvider.getOption(ChatOption.AGENT))
                .build();
    }
}