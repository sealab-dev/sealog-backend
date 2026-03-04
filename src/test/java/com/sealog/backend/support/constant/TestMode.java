package com.sealog.backend.support.constant;

import lombok.experimental.UtilityClass;

/**
 * 테스트 모드를 관리하는 상수 클래스
 */
@UtilityClass
public class TestMode {

    public static final String NORMAL = "NORMAL"; // 일반 테스트
    public static final String PERFORMANCE = "PERFORMANCE"; // 성능 테스트
}
