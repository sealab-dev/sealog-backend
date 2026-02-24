package com.sealog.backend.domain.base.init;

import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.entity.StackGroup;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalStackInitializer implements ApplicationRunner {

    private final StackRepository stackRepository;

    @Override
    public void run(ApplicationArguments args) {
        createTestStack();
    }

    private void createTestStack() {
        String name = "Test";
        if (stackRepository.findByName(name).isEmpty()) {
            Stack stack = Stack.builder()
                    .name(name)
                    .stackGroup(StackGroup.ETC)
                    .build();

            stackRepository.save(stack);
            log.info("로컬 테스트 스택 생성: {}", name);
        }
    }
}