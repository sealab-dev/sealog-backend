package com.sealog.backend.support.base.config;

import com.sealog.backend.support.base.container.TestIntegrationContainer;
import com.sealog.backend.support.base.container.TestPersistenceContainer;
import com.sealog.backend.support.component.TestDataFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

/**
 * 통합 테스트에 사용할 데이터 관련 기능을 정의하는 추상 클래스
 */
@AutoConfigureMockMvc
public abstract class TestIntegrationConfig extends TestIntegrationContainer {

}
