package com.sealog.backend.support.base;

import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.config.TestInitConfig;
import com.sealog.backend.support.config.TestPerformanceInitConfig;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

/**
 * 성능 테스트의 공통 기능을 관리하는 클래스
 */
@Tag(TestMode.PERFORMANCE)
@Import({TestInitConfig.class, TestPerformanceInitConfig.class, TestDataFactory.class})
public abstract class TestPerformanceBase extends TestContainerBase {

}
