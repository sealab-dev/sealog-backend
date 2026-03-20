package com.sealog.backend.support.base.test;

import com.sealog.backend.infra.orm.querydsl.QueryDslConfig;
import com.sealog.backend.support.base.container.TestPersistenceContainer;
import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

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
@Import({TestDataFactory.class, QueryDslConfig.class})
@Sql(
        // Spring 컨텍스트 로딩(=JPA 테이블 생성) 완료 이후, 첫 번째 테스트 메서드 실행 이전에 실행
        scripts = "/sql/index.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 테스트에선 Testcontainer를 무시하고 내장 H2로 교체 시도
public abstract class PersistenceTest extends TestPersistenceContainer {

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
