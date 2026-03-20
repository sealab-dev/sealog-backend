package com.sealog.backend.support.base.test;


import com.sealog.backend.support.base.container.TestIntegrationContainer;
import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 통합 테스트
 * Controller → Service → Repository 전체 레이어를 관통하는 통합 테스트.
 */

@Tag(TestMode.INTEGRATION)
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "jwt.secret=YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=",
        "jwt.access-token-validity=3600000",
        "jwt.refresh-token-validity=86400000"
})
public abstract class IntegrationTest extends TestIntegrationContainer {

    // 사용 의존성
    @Autowired TestDataFactory testDataFactory;
    @Autowired StringRedisTemplate stringRedisTemplate;

    /**
     * 각 테스트 클래스 종료 후, 테이블 재생성 (TRUNCATE)
     */
    @BeforeEach
    protected void clearDatabase() {

        // Database Table 초기화
        testDataFactory.clearTable();

        // Redis 초기화
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
}
