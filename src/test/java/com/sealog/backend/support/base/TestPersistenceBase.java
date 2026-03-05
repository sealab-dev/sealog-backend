package com.sealog.backend.support.base;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.support.extension.ExecutionTimeExtension;
import com.sealog.backend.support.component.TestDataFactory;
import com.sealog.backend.support.constant.TestMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 일반 테스트의 공통 기능을 관리하는 클래스
 */

@Tag(TestMode.PERSISTENCE)
@Import(TestDataFactory.class)
@ExtendWith(ExecutionTimeExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public abstract class TestPersistenceBase extends TestContainerBase {

    @Autowired TestDataFactory testDataFactory;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
        adminUser = testDataFactory.createUser(UserRole.ADMIN);
    }
}
