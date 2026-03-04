package com.sealog.backend.support.config;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.support.component.TestDataFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;


/**
 * 테스트 전 필요 정보를 초기화를 위한 클래스
 */
@TestConfiguration // 자동으로 빈 등록이 되지 않음
@RequiredArgsConstructor
public class TestPerformanceInitConfig {

    // 사용 의존성
    private final TestDataFactory testDataFactory;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * 테스트 전 초기화 작업
     */
    @PostConstruct
    public void init() {

        // 1. 일반 유저 생성
        testDataFactory.createTestUsers(100000, UserRole.USER);

        // 2. 게시글 생성
        User user = userRepository.findById(1L).orElseThrow(); // id = 0 사용자 기준으로 작성
        testDataFactory.createTestPosts(100000, PostStatus.PUBLISHED, user);
    }

}
