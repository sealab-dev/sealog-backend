package com.sealog.backend.support.base.test;

import com.sealog.backend.support.base.config.TestGlobalConfig;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 일반 테스트의 공통 기능을 관리하는 클래스
 */

@Tag(TestMode.UNIT)
@ExtendWith({MockitoExtension.class})
public abstract class UnitTest extends TestGlobalConfig {

    @Test
    void warmup() {
    }
}
