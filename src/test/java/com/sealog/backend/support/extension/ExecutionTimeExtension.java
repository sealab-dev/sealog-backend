package com.sealog.backend.support.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 각 테스트 메서드의 전체 수행 시간을 로깅하는 JUnit 5 Extension.
 */
public class ExecutionTimeExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeExtension.class);
    private static final String START_TIME = "startTime";

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ExecutionTimeExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getStore(NAMESPACE)
                .put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        long start = context.getStore(NAMESPACE)
                .get(START_TIME, Long.class);
        long duration = System.currentTimeMillis() - start;
        log.info("[Test] {} | {}ms", context.getDisplayName(), duration);
    }
}