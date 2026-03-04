package com.sealog.backend.support.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 테스트에 사용할 인프라 컨테이너를 정의하는 추상 클래스
 */
@SpringBootTest
@Testcontainers
abstract class TestContainerBase {

    // 상수
    private static final int PORT_REDIS = 6379;

    // 컨테이너
    @Container
    @ServiceConnection
    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>(
            System.getenv().getOrDefault("IMAGE_DATABASE", "mariadb:11.4.8"));


    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(
            System.getenv().getOrDefault("IMAGE_REDIS", "redis:7.2"))
            .withExposedPorts(PORT_REDIS);

}
