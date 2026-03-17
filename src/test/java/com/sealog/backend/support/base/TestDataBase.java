package com.sealog.backend.support.base;

import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.extension.ExecutionTimeExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

/**
 * 테스트에 사용할 데이터베이스 관련 기능을 정의하는 추상 클래스
 */

@ExtendWith(ExecutionTimeExtension.class)
@Import(TestDataFactory.class)
@Sql(
        // Spring 컨텍스트 로딩(=JPA 테이블 생성) 완료 이후, 첫 번째 테스트 메서드 실행 이전에 실행
        scripts = "/sql/fulltext-index.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
abstract class TestDatabaseBase {

}
