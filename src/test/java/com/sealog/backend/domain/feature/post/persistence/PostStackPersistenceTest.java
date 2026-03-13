package com.sealog.backend.domain.feature.post.persistence;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.entity.PostStack;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.repository.PostStackRepository;
import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.support.base.TestPersistenceBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * PostStack 쿼리 성능 측정 테스트
 * - 성공/실패 검증이 아닌 실행 시간 측정 목적
 * - ExecutionTimeExtension이 각 테스트 메서드 수행 시간을 자동 로깅
 * - @BeforeAll로 초기 데이터를 클래스 전체에서 1회만 생성, @AfterAll에서 정리
 */
@DisplayName("PostStack 쿼리 성능 측정")
class PostStackPersistenceTest extends TestPersistenceBase {

    @Autowired TestDataFactory testDataFactory;
    @Autowired PostStackRepository postStackRepository;
    @Autowired PostRepository postRepository;

    private User testUser;
    private List<Stack> stacks;

    @BeforeAll
    void setUpAll() {

        // 1. 사용자 1명 생성
        testUser = testDataFactory.createUser(UserRole.USER);

        // 2. 스택 5개 생성
        stacks = List.of(
                testDataFactory.createStack(StackGroup.LANGUAGE),
                testDataFactory.createStack(StackGroup.LANGUAGE),
                testDataFactory.createStack(StackGroup.FRAMEWORK),
                testDataFactory.createStack(StackGroup.FRAMEWORK),
                testDataFactory.createStack(StackGroup.TOOL)
        );

        // 3. 공개 게시글 10만개 생성
        testDataFactory.createTestPosts(100, PostStatus.PUBLISHED, testUser);

        // 4. PostStack 매핑 생성 (게시글당 스택 1개, 스택별 약 2만개 배분)
        List<Post> posts = postRepository.findAll();
        List<PostStack> mappings = new ArrayList<>();

        for (int i = 0; i < posts.size(); i++) {
            mappings.add(PostStack.builder()
                    .post(posts.get(i))
                    .stack(stacks.get(i % stacks.size()))
                    .sortOrder(1)
                    .build());
        }

        postStackRepository.saveAll(mappings);
    }

    @Test
    @Order(0)
    void warmUp() {
        // 아무것도 안 함, JVM 웜업용
    }

    // =====================================================================
    // 성능 측정
    // =====================================================================

    @Test
    @DisplayName("findStacksWithPublicPostCount - 전체 스택별 게시글 수 집계 (페이징 없음)")
    void findStacksWithPublicPostCount() {
        List<Object[]> result = postStackRepository.findStacksWithPublicPostCount();
        System.out.println("[결과] 집계된 스택 수: " + result.size());
    }

    @Test
    @DisplayName("findStacksWithPublicPostCountByUser - 특정 사용자 스택별 게시글 수 집계 (nickname 조인)")
    void findStacksWithPublicPostCountByUser() {
        List<Object[]> result = postStackRepository.findStacksWithPublicPostCountByUser(testUser.getNickname());
        System.out.println("[결과] 집계된 스택 수: " + result.size());
    }

    @Test
    @DisplayName("findPopularStacks - 인기 스택 상위 3개 조회 (LIMIT 적용)")
    void findPopularStacks() {
        List<Object[]> result = postStackRepository.findPopularStacks(3);
        System.out.println("[결과] 조회된 인기 스택 수: " + result.size());
    }
}
