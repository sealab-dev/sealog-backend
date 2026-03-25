package com.sealog.backend.support.base.test;


import com.sealog.backend.infra.orm.querydsl.QueryDslConfig;
import com.sealog.backend.support.base.config.TestGlobalConfig;
import com.sealog.backend.support.base.container.*;
import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * 통합 테스트 베이스 클래스
 */

@Tag(TestMode.INTEGRATION)
@AutoConfigureMockMvc
@Import({TestDataFactory.class, QueryDslConfig.class, TestMariaDBContainer.class, TestRedisContainer.class})
@SpringBootTest
public abstract class IntegrationTest extends TestGlobalConfig {

    // 사용 의존성
    @Autowired protected TestDataFactory testDataFactory;
    @Autowired protected StringRedisTemplate stringRedisTemplate;

    // warmUp 수행 여부
    private static volatile boolean warmedUp = false;

    @BeforeEach
    protected void setup() throws Exception {
        // 매 테스트 실행 전 데이터 초기화
        clearDatabase();
    }

    @Test
    void warmup() {
    }

    protected void clearDatabase() {
        testDataFactory.clearTable();
        if (stringRedisTemplate.getConnectionFactory() != null) {
            stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        }
    }



}
