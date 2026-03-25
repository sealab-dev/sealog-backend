package com.sealog.backend.support.base.container;

import com.sealog.backend.support.base.container.legacy.TestContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MariaDBContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestMariaDBContainer {

    @Bean
    @ServiceConnection
    public MariaDBContainer<?> mariaDBContainer() {
        return new MariaDBContainer<>(TestContainer.DEFAULT_IMAGE_DATABASE)
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
    }

}
