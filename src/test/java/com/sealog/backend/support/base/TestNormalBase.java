package com.sealog.backend.support.base;

import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.config.TestInitConfig;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

/**
 * 일반 테스트의 공통 기능을 관리하는 클래스
 */

@Tag(TestMode.NORMAL)
@Import({TestInitConfig.class, TestDataFactory.class})
public abstract class TestNormalBase extends TestContainerBase {

}
