package com.sealog.backend.support.constant;

import lombok.experimental.UtilityClass;

/**
 * 테스트 모드를 관리하는 상수 클래스
 */
@UtilityClass
public class TestMode {

    /**
     * [유닛 테스트]
     * Mock 데이터 기반
     */
    public static final String UNIT = "UNIT";
    /**
     * [영속성 테스트]
     * 실제 DB 기반으로 쿼리의 성능은 어떤가?
     */
    public static final String PERSISTENCE = "PERSISTENCE";
    /**
     * [통합 테스트]
     * 실제 빈을 기반으로 전체 플로우가 정상적으로 동작하는가?
     */
    public static final String INTEGRATION = "INTEGRATION";
}
