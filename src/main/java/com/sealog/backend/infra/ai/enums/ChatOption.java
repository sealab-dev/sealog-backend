package com.sealog.backend.infra.ai.enums;

/**
 * AI 요청 옵션 값을 관리하는 열거형 상수 클래스
 * @author kcw
 * @since 2026-02-06
 */
public enum ChatOption {
    STRICT,         // 정확성 추구
    MODERATE,       // 제한된 다양성
    CREATIVE,       // 높은 다양성
    AGENT           // AI AGENT 전용
}
