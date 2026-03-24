package com.sealog.backend.support.base.container.legacy;

import com.sealog.backend.support.base.config.TestGlobalConfig;
import com.sealog.backend.support.constant.TestContainer;
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
    static final MariaDBContainer<?> MARIA_DB =
            new MariaDBContainer<>(TestContainer.DEFAULT_IMAGE_DATABASE)
                    .withDatabaseName(TestContainer.DEFAULT_DATABASE_NAME)
                    .withUsername(TestContainer.DEFAULT_DATABASE_USERNAME)
                    .withPassword(TestContainer.DEFAULT_DATABASE_PASSWORD)
                    .withUrlParam("serverTimezone", "Asia/Seoul")
                    .withUrlParam("characterEncoding", "UTF-8")
                    .withCommand(
                            "--innodb-flush-log-at-trx-commit=2",  // commit 시 fsync 제거 (OS 버퍼에 쓰기만)
                            "--innodb-doublewrite=OFF",             // 이중 쓰기 버퍼 비활성화 (데이터 파일 쓰기 절반으로 감소, 운영 환경 비권장)
                            "--innodb-buffer-pool-size=512M",       // dirty page 수용 공간 확대 (DB 버퍼 공간 증가로 삽입 시간 개선)
                            "--max-allowed-packet=128M",            // MariaDB로 전송하는 단일 패킷 최대 크기
                            "--innodb-ft-min-token-size=2"          // 한글 2글자 검색을 위해 MariaDB 설정
                    );

    @Container
    @ServiceConnection
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(TestContainer.DEFAULT_IMAGE_REDIS)
                    .withExposedPorts(TestContainer.DEFAULT_REDIS_PORT);

}
