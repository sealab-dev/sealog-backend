package com.sealog.backend.support.base;

import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestContainer;
import com.sealog.backend.support.constant.TestMode;
import com.sealog.backend.support.constant.TestSql;
import com.sealog.backend.support.extension.ExecutionTimeExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 통합 테스트
 * Controller → Service → Repository 전체 레이어를 관통하는 통합 테스트.
 * 각 테스트는 @Transactional로 격리되며 종료 시 자동 롤백된다.
 */
@Tag(TestMode.INTEGRATION)
@Import(TestDataFactory.class)
@ExtendWith(ExecutionTimeExtension.class)
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "jwt.secret=YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=",
        "jwt.access-token-validity=3600000",
        "jwt.refresh-token-validity=86400000"
})
@AutoConfigureMockMvc
public abstract class TestIntegrationBase extends TestContainerBase {

    // 사용 의존성
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 각 테스트 클래스 종료 후, 테이블 재생성 (TRUNCATE)
     * DELETE 기반 삭제보다 빠르고, AUTO_INCREMENT 초기화
     */
    @BeforeEach
    void clearDatabase() {

        // 1. FK Constraint 비활성화
        jdbcTemplate.execute(TestSql.FOREIGN_KEY_CHECKS_INACTIVATION);

        // 2. 테이블 행 일괄 삭제
        jdbcTemplate
                .queryForList(TestSql.SELECT_TABLE_NAMES, String.class, TestContainer.DEFAULT_DATABASE_NAME)
                .forEach(tableName -> jdbcTemplate.execute(TestSql.TRUNCATE_TABLE + tableName));

        // 3. FK Constraint 활성화
        jdbcTemplate.execute(TestSql.FOREIGN_KEY_CHECKS_ACTIVATION);
    }
}
