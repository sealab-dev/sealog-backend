package com.sealog.backend.support.base.container.legacy;

import com.sealog.backend.support.base.config.TestGlobalConfig;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

/**
 * 통합 테스트에 사용할 인프라 컨테이너를 정의하는 추상 클래스
 */
public abstract class TestIntegrationContainer extends TestGlobalConfig {

    // 컨테이너
    static final MariaDBContainer<?> MARIA_DB;
    static final GenericContainer<?> REDIS;

    // static 블록
    static {
        MARIA_DB = TestContainer.getAndStartMariaDBContainer();
        REDIS = TestContainer.getAndStartRedisContainer();
    }

    /**
     * 컨테이너가 할당한 동적 포트·주소를 Spring Environment에 주입한다.
     * application-test.yml 의 datasource/redis 기본값을 런타임에 덮어쓴다.
     */
    @DynamicPropertySource
    static void overrideContainerProperties(DynamicPropertyRegistry registry) {
        TestContainer.registerMariaDb(MARIA_DB, registry);
        TestContainer.registerRedis(REDIS, registry);
    }

}
