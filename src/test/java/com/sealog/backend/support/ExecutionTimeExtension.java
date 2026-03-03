package com.sealog.backend.support;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 각 테스트 메서드의 전체 수행 시간을 로깅하는 JUnit 5 Extension.
 *
 * <pre>
 *   @ExtendWith(ExecutionTimeExtension.class)
 *   class MyTest { ... }
 * </pre>
 *
 * 출력 예) [Test] 성공 - 올바른 이메일/비밀번호 → 200, access/refresh 쿠키 발급, 프로필 반환 | 312ms
 */
public class ExecutionTimeExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeExtension.class);
    private static final String START_TIME = "startTime";

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.GLOBAL)
                .put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        long start = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(START_TIME, Long.class);
        long duration = System.currentTimeMillis() - start;
        log.info("[Test] {} | {}ms", context.getDisplayName(), duration);
    }
}
