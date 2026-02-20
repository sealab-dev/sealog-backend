package com.sealog.backend.infra.ai.constant;

import lombok.experimental.UtilityClass;

/**
 * AI 채팅 프롬포트 관리 상수 클래스
 * @author kcw
 * @since 2026-02-19
 */

@UtilityClass
public class AiChatPrompt {

    // 사용 상수
    public static final String DATA_INPUT = "${DATA_INPUT}";
    public static final String DATA_PREV = "${DATA_PREV}";
    public static final String AMOUNT = "${AMOUNT}";

    // 뉴스 프롬프트 - default
    public static String PROMPT_NEWS_DEFAULT = """

        ### 역할
            당신은 뉴스 내용정리 AI 어시스턴트 입니다.
            뉴스의 핵심 키워드를 선정하고, 해당 키워드가 이 뉴스에서 어떤 의미를 갖는지 해석합니다.

        
    """;

    // 뉴스 프롬프트 - user
    public static String PROMPT_NEWS_USER = """

        ### 현재 뉴스:
            ${DATA_INPUT}

        ### 이전 분석 키워드 목록:
            ${DATA_PREV}

        ### 분석 키워드 개수:
            ${AMOUNT}
    """;

    // 에이전트 프롬프트 - default
    public static String PROMPT_AGENT_DEFAULT = """
        wddw
    """;

    // 에이전트 프롬프트 - user
    public static String PROMPT_AGENT_USER = """
        dwdwd
    """;
}
