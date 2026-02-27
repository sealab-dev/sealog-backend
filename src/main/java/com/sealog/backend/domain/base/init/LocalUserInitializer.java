package com.sealog.backend.domain.base.init;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createAdminUser();
        createTestUser();
    }

    private void createAdminUser() {
        String email = "admin@local.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            User admin = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("admin1234"))
                    .name("김관리")
                    .nickname("관리자")
                    .role(UserRole.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("로컬 관리자 계정 생성: {} / admin1234", email);
        }
    }

    private void createTestUser() {
        String email = "test@local.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("test1234"))
                    .name("박유저")
                    .nickname("테스터")
                    .role(UserRole.USER)
                    .build();

            userRepository.save(user);
            log.info("로컬 일반 계정 생성: {} / test1234", email);
        }
    }
}