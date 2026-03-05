package com.sealog.backend.support.base;

import com.sealog.backend.support.extension.ExecutionTimeExtension;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * 일반 테스트의 공통 기능을 관리하는 클래스
 */

@Tag(TestMode.UNIT)
@ExtendWith({MockitoExtension.class, ExecutionTimeExtension.class})
@ActiveProfiles("test")
public abstract class TestUnitBase  {

}
