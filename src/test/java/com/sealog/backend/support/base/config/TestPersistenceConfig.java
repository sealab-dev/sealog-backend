package com.sealog.backend.support.base.config;

import com.sealog.backend.support.base.container.TestPersistenceContainer;
import com.sealog.backend.support.component.TestDataFactory;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

/**
 * 테스트에 사용할 데이터 관련 기능을 정의하는 추상 클래스
 */
@Import({TestDataFactory.class})
@Sql(
        // Spring 컨텍스트 로딩(=JPA 테이블 생성) 완료 이후, 첫 번째 테스트 메서드 실행 이전에 실행
        scripts = "/sql/index.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 테스트에선 Testcontainer를 무시하고 내장 H2로 교체 시도
public abstract class TestPersistenceConfig extends TestPersistenceContainer {

}
