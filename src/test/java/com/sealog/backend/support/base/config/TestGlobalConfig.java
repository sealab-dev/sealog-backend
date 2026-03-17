package com.sealog.backend.support.base.config;

import com.sealog.backend.support.component.TestTextGenerator;
import com.sealog.backend.support.extension.ExecutionTimeExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 모든 테스트가 공용으로 사용할 설정을 정의하는 추상 클래스
 */
@ActiveProfiles("test")
@Import({TestTextGenerator.class})
@ExtendWith(ExecutionTimeExtension.class) // 메소드 별 시간 측정
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // @Order 기반 실행 순서
public abstract class TestGlobalConfig {

}
