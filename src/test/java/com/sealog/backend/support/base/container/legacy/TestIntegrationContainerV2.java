package com.sealog.backend.support.base.container.legacy;

import com.sealog.backend.support.base.config.TestGlobalConfig;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * [비교용] @Testcontainers + static @Container 기반 컨테이너 관리 클래스
 */
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class TestIntegrationContainerV2 extends TestGlobalConfig {

    // 컨테이너 (static: @Testcontainers 가 BeforeAll/AfterAll 에서 클래스마다 기동·종료)
    @Container
    @ServiceConnection
    static final MariaDBContainer<?> MARIA_DB = TestContainer.MARIA_DB;

    @Container
    @ServiceConnection
    static final GenericContainer<?> REDIS = TestContainer.REDIS;

}
