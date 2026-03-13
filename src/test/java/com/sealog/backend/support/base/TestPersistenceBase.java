package com.sealog.backend.support.base;

import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestContainer;
import com.sealog.backend.support.constant.TestMode;
import com.sealog.backend.support.constant.TestSql;
import com.sealog.backend.support.extension.ExecutionTimeExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 영속성 테스트 공통 기반 클래스
 *
 * 특징:
 * - @DataJpaTest: JPA 레이어만 로드 (경량 컨텍스트)
 * - @TestInstance(PER_CLASS): @BeforeAll / @AfterAll non-static 허용, @Autowired 필드 사용 가능
 * - ExecutionTimeExtension: 각 테스트 메서드 수행 시간 자동 로깅
 * - @AfterAll clearDatabase: 클래스 종료 후 테이블 전체 TRUNCATE
 */
@Slf4j
@Tag(TestMode.PERSISTENCE)
@Import(TestDataFactory.class)
@ExtendWith(ExecutionTimeExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestPersistenceBase extends TestContainerBase {

    // 사용 의존성
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 클래스 종료 후 테이블 전체 초기화
     * - @BeforeAll로 커밋된 데이터 정리
     * - @BeforeEach(롤백) 방식 사용 시에도 no-op으로 안전
     */
    @AfterAll
    void clearDatabase() {
        jdbcTemplate.execute(TestSql.FOREIGN_KEY_CHECKS_INACTIVATION);
        jdbcTemplate
                .queryForList(TestSql.SELECT_TABLE_NAMES, String.class, TestContainer.DEFAULT_DATABASE_NAME)
                .forEach(tableName -> jdbcTemplate.execute(TestSql.TRUNCATE_TABLE + tableName));
        jdbcTemplate.execute(TestSql.FOREIGN_KEY_CHECKS_ACTIVATION);
        log.warn("영속성 테스트 데이터 삭제 성공");
    }
}
