package com.sealog.backend.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 기반 통합 테스트 베이스 클래스
 *
 * <p>MariaDB / Redis 컨테이너를 JVM 당 한 번만 기동하는 싱글턴 패턴을 사용한다.
 * static 초기화 블록에서 컨테이너를 시작하므로, 이 클래스를 상속받는 모든 테스트가
 * 동일한 컨테이너 인스턴스를 공유하여 테스트 전체 실행 시간을 단축한다.
 *
 * <pre>
 * ┌────────────────────────────────────────────────────┐
 * │  사용법                                             │
 * │                                                    │
 * │  @SpringBootTest                                   │
 * │  @ActiveProfiles("test")                           │
 * │  class MyIntegrationTest extends AbstractContainerTest { │
 * │      // 테스트 작성                                │
 * │  }                                                 │
 * └────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>컨테이너 설정 (변경 필요 시 이 클래스만 수정):
 * <ul>
 *   <li>MariaDB: mariadb:11, DB=sealog_test</li>
 *   <li>Redis:   redis:7-alpine, port=6379</li>
 * </ul>
 */
public abstract class AbstractContainerTest {

    // ── MariaDB ────────────────────────────────────────────────────────────
    static final MariaDBContainer<?> MARIA_DB;

    // ── Redis ──────────────────────────────────────────────────────────────
    @SuppressWarnings("rawtypes")
    static final GenericContainer REDIS;

    /*
     * JVM 당 한 번만 컨테이너를 기동한다 (싱글턴 컨테이너 패턴).
     * 테스트가 끝나면 Testcontainers의 Ryuk 컨테이너가 자동으로 정리한다.
     */
    static {
        MARIA_DB = new MariaDBContainer<>(DockerImageName.parse("mariadb:11"))
                .withDatabaseName("sealog_test")
                .withUsername("test")
                .withPassword("test");

        REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);

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
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");

        // ── Redis ─────────────────────────────────────────────────────────
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(6379)));
    }
}
