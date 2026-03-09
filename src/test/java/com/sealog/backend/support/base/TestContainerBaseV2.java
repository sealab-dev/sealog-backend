package com.sealog.backend.support.base;

import com.sealog.backend.support.constant.TestContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 테스트에 사용할 인프라 컨테이너를 정의하는 추상 클래스
 */
@ActiveProfiles("test")
@Testcontainers
abstract class TestContainerBaseV2 {

    // 컨테이너
    static final MariaDBContainer<?> MARIA_DB;
    static final GenericContainer<?> REDIS;

    // static 블록
    static {

        MARIA_DB = new MariaDBContainer<>(TestContainer.DEFAULT_IMAGE_DATABASE)
                .withDatabaseName(TestContainer.DEFAULT_DATABASE_NAME)
                .withUsername(TestContainer.DEFAULT_DATABASE_USERNAME)
                .withPassword(TestContainer.DEFAULT_DATABASE_PASSWORD);

        REDIS = new GenericContainer<>(TestContainer.DEFAULT_IMAGE_REDIS)
                .withExposedPorts(TestContainer.DEFAULT_REDIS_PORT);

        MARIA_DB.start();
        REDIS.start();
    }

    /**
     * 컨테이너가 할당한 동적 포트·주소를 Spring Environment에 주입한다.
     * application-test.yml 의 datasource/redis 기본값을 런타임에 덮어쓴다.
     */
    @DynamicPropertySource
    static void overrideContainerProperties(DynamicPropertyRegistry registry) {
        // ── MariaDB ──────────────────────────────────────────────────────
        registry.add("spring.datasource.url",
                () -> MARIA_DB.getJdbcUrl() + "?serverTimezone=Asia/Seoul&characterEncoding=UTF-8");
        registry.add("spring.datasource.username", MARIA_DB::getUsername);
        registry.add("spring.datasource.password", MARIA_DB::getPassword);

        // ── Redis ─────────────────────────────────────────────────────────
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(TestContainer.DEFAULT_REDIS_PORT)));
    }

}
