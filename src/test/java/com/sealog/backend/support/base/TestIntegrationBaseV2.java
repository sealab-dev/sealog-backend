package com.sealog.backend.support.base;

import com.sealog.backend.support.extension.ExecutionTimeExtension;
import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 통합 테스트
 * Controller → Service → Repository 전체 레이어를 관통하는 통합 테스트.
 * 각 테스트는 @Transactional로 격리되며 종료 시 자동 롤백된다.
 */
@Tag(TestMode.INTEGRATION)
@Import(TestDataFactory.class)
@ExtendWith(ExecutionTimeExtension.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class TestIntegrationBaseV2 extends TestContainerBaseV2 {

}
