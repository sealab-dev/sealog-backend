package com.sealog.backend.support.base.container.legacy;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

/**
 * 테스트 컨테이너 관련 상수를 관리하는 클래스
 */
@UtilityClass
public class TestContainer {

    // 컨테이너 내부에 사용하는 상수
    public static final String DEFAULT_IMAGE_DATABASE = System.getenv().getOrDefault("IMAGE_DATABASE", "mariadb:11.4.8");
    public static final String DEFAULT_IMAGE_REDIS = System.getenv().getOrDefault("IMAGE_REDIS", "redis:7.2");
    public static final String DEFAULT_DATABASE_NAME = "sealog_test";
    public static final String DEFAULT_DATABASE_USERNAME = "test";
    public static final String DEFAULT_DATABASE_PASSWORD = "test";
    public static final int DEFAULT_REDIS_PORT = 6379;

    // 컨테이너 상수
    public static final MariaDBContainer<?> MARIA_DB =
            new MariaDBContainer<>(TestContainer.DEFAULT_IMAGE_DATABASE)
                    .withDatabaseName(TestContainer.DEFAULT_DATABASE_NAME)
                    .withUsername(TestContainer.DEFAULT_DATABASE_USERNAME)
                    .withPassword(TestContainer.DEFAULT_DATABASE_PASSWORD)
                    .withCommand(
                            "--innodb-flush-log-at-trx-commit=2",  // commit 시 fsync 제거 (OS 버퍼에 쓰기만)
                            "--innodb-doublewrite=OFF",             // 이중 쓰기 버퍼 비활성화 (데이터 파일 쓰기 절반으로 감소, 운영 환경 비권장)
                            "--innodb-buffer-pool-size=512M",       // dirty page 수용 공간 확대 (DB 버퍼 공간 증가로 삽입 시간 개선)
                            "--max-allowed-packet=128M",            // MariaDB로 전송하는 단일 패킷 최대 크기
                            "--innodb-ft-min-token-size=2"          // 한글 2글자 검색을 위해 MariaDB 설정
                    );

    public static final GenericContainer<?> REDIS =
            new GenericContainer<>(TestContainer.DEFAULT_IMAGE_REDIS)
                    .withExposedPorts(TestContainer.DEFAULT_REDIS_PORT);


    // 컨테이너 인스턴스 제공 및 시작 메소드
    static MariaDBContainer<?> getAndStartMariaDBContainer() {
        MARIA_DB.start();
        return MARIA_DB;
    }

    static GenericContainer<?> getAndStartRedisContainer() {
        REDIS.start();
        return REDIS;
    }


    // 컨테이너 설정 제공 메소드
    static void registerMariaDb(
            MariaDBContainer<?> container,
            DynamicPropertyRegistry registry
    ) {
        registry.add("spring.datasource.url", () -> container.getJdbcUrl() + "?serverTimezone=Asia/Seoul&characterEncoding=UTF-8");
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    static void registerRedis(
            GenericContainer<?> container,
            DynamicPropertyRegistry registry
    ) {
        registry.add("spring.data.redis.host", container::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(container.getMappedPort(DEFAULT_REDIS_PORT)));
    }
}
