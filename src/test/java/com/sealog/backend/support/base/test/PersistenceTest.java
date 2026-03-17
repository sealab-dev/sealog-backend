package com.sealog.backend.support.base.test;

import com.sealog.backend.support.base.config.TestPersistenceConfig;
import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * 영속성 테스트 기반 클래스
 *
 * 사용 목적:
 * - 대량 데이터 기반 쿼리 성능·정확성 검증
 * - @BeforeAll로 생성한 데이터를 테스트 간 유지 (clearDatabase no-op)
 * - 테스트 종료 시 truncateAll()로 직접 정리
 */
@Tag(TestMode.PERSISTENCE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
public abstract class PersistenceTest extends TestPersistenceConfig {

    // 사용 의존성
    @Autowired
    private TestDataFactory testDataFactory;

    /**
     * 각 테스트 클래스 종료 후, 테이블 재생성 (TRUNCATE)
     */
    @BeforeAll
    protected void truncateAll() {
        testDataFactory.clearTable();
    }
}
