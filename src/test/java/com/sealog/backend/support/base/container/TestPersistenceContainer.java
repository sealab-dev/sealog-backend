package com.sealog.backend.support.base.container;

import com.sealog.backend.support.base.config.TestGlobalConfig;
import com.sealog.backend.support.constant.TestContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 영속 테스트에 사용할 인프라 컨테이너를 정의하는 추상 클래스
 */
@Testcontainers
public abstract class TestPersistenceContainer extends TestGlobalConfig {

    // 컨테이너
    static final MariaDBContainer<?> MARIA_DB;

    // static 블록
    static {

        MARIA_DB = new MariaDBContainer<>(TestContainer.DEFAULT_IMAGE_DATABASE)
                .withDatabaseName(TestContainer.DEFAULT_DATABASE_NAME)
                .withUsername(TestContainer.DEFAULT_DATABASE_USERNAME)
                .withPassword(TestContainer.DEFAULT_DATABASE_PASSWORD)
                .withCommand(
                    "--innodb-flush-log-at-trx-commit=2",  // commit 시 fsync 제거 (OS 버퍼에 쓰기만)
                        "--innodb-doublewrite=OFF",             // 이중 쓰기 버퍼 비활성화 (데이터 파일 쓰기 절반으로 감소, 운영 환경 비권장)
                        "--innodb-buffer-pool-size=512M",       // dirty page 수용 공간 확대
                        "--innodb-ft-min-token-size=2"          // 한글 2글자 검색을 위해 MariaDB 설정
                );

        MARIA_DB.start();
    }

    /**
     * 컨테이너가 할당한 동적 포트·주소를 Spring Environment에 주입한다.
     * application-test.yml 의 datasource/redis 기본값을 런타임에 덮어쓴다.
     */
    @DynamicPropertySource
    static void overrideContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> MARIA_DB.getJdbcUrl() + "?serverTimezone=Asia/Seoul&characterEncoding=UTF-8");
        registry.add("spring.datasource.username", MARIA_DB::getUsername);
        registry.add("spring.datasource.password", MARIA_DB::getPassword);
    }

}
