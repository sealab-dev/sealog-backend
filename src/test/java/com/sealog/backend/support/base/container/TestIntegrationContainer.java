package com.sealog.backend.support.base.container;

import com.sealog.backend.support.base.config.TestPersistenceConfig;
import com.sealog.backend.support.constant.TestContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 통합 테스트에 사용할 인프라 컨테이너를 정의하는 추상 클래스
 */
@Testcontainers
public abstract class TestIntegrationContainer extends TestPersistenceConfig {

    // 컨테이너
    static final GenericContainer<?> REDIS;

    // static 블록
    static {
        REDIS = new GenericContainer<>(TestContainer.DEFAULT_IMAGE_REDIS)
                .withExposedPorts(TestContainer.DEFAULT_REDIS_PORT);

        REDIS.start();
    }

    /**
     * 컨테이너가 할당한 동적 포트·주소를 Spring Environment에 주입한다.
     * application-test.yml 의 datasource/redis 기본값을 런타임에 덮어쓴다.
     */
    @DynamicPropertySource
    static void overrideContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(TestContainer.DEFAULT_REDIS_PORT)));
    }

}
