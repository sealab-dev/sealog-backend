package com.sealog.backend.support.component;


import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.series.repository.SeriesRepository;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/**
 * 테스트 데이터 생성을 위한 클래스
 */

@Transactional
@TestComponent // 운영 환경에서 생성 방지
@RequiredArgsConstructor
public class TestDataFactory {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final StackRepository stackRepository;

    // 패스워드 인코더 직접 주입(스프링 의존성 제거)
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    // =========================================================
    // 단일 생성
    // =========================================================

    /**
     * User 단일 생성
     * @param role 회원 권한
     * @return 저장된 User 엔티티 (ID 포함)
     */
    public User createUser(UserRole role) {
        return userRepository.save(
                createEntity(userRepository::count, idx -> buildUser(idx, "password", role))
        );
    }


    /**
     * Post 단일 생성
     * @param user   작성자
     * @param status 게시글 상태
     * @return 저장된 Post 엔티티 (ID 포함)
     */
    public Post createPost(User user, PostStatus status) {
        return postRepository.save(
                createEntity(postRepository::count, idx -> buildPost(user, status, idx))
        );
    }


    /**
     * Post 단일 생성
     * @param user    작성자
     * @param series 게시글 소속 시리즈
     * @param status  게시글 상태
     * @return 저장된 Post 엔티티 (ID 포함)
     */
    public Post createPost(User user, Series series, PostStatus status) {

        // 1. 엔티티 생성 및 시리즈 삽입
        Post post = createEntity(postRepository::count, idx -> buildPost(user, status, idx));
        post.addToSeries(series);

        // 2. 생성 및 반환
        return postRepository.save(post);
    }

    /**
     * Series 단일 생성
     * @param user     소유자
     * @param isPublic 공개 여부
     * @return 저장된 Series 엔티티 (ID 포함)
     */
    public Series createSeries(User user, boolean isPublic) {
        return seriesRepository.save(
                createEntity(seriesRepository::count, idx -> buildSeries(user, isPublic, idx))
        );
    }


    /**
     * Stack 단일 생성
     * @param stackGroup 스택 그룹
     * @return 저장된 Stack 엔티티 (ID 포함)
     */
    public Stack createStack(StackGroup stackGroup) {
        return stackRepository.save(
                createEntity(stackRepository::count, idx -> buildStack(stackGroup, idx))
        );
    }


    // =========================================================
    // 대량 생성
    // =========================================================

    /**
     * TEST User 생성
     * amount = 10 -> 테스트000000~테스트000009까지 생성
     * @param amount 생성 수량
     * @param role   회원 권한
     */
    public void createTestUsers(int amount, UserRole role) {

        // 1. User 생성
        List<User> users = createEntities(
                amount,
                userRepository::count,
                idx -> buildUser(idx, "password", role)
        );

        // 2. 삽입 수행 (시간 측정)
        LogUtils.runAndShowCostLog("테스트 사용자 저장", () -> userRepository.saveAll(users));
    }

    /**
     * TEST Post 생성
     * amount = 10 -> 제목000000~제목000009까지 생성
     * @param amount 생성 수량
     * @param status 블로그 게시글 상태
     * @param user   블로그 작성자 회원 엔티티
     */
    public void createTestPosts(int amount, PostStatus status, User user) {

        // 1. Post 생성
        List<Post> posts = createEntities(
                amount,
                postRepository::count,
                idx -> buildPost(user, status, idx)
        );

        // 2. 삽입 수행 (시간 측정)
        LogUtils.runAndShowCostLog("테스트 블로그 저장", () -> postRepository.saveAll(posts));
    }


    /**
     * TEST Series 생성
     * amount = 10 -> 시리즈000000~시리즈000009까지 생성
     * @param amount    생성 수량
     * @param isPublic  공개 여부
     * @param user      시리즈 생성 사용자 엔티티
     */
    public void createTestSeries(int amount, boolean isPublic, User user) {

        // 1. Series 생성
        List<Series> seriesList = createEntities(
                amount,
                seriesRepository::count,
                idx -> buildSeries(user, isPublic, idx)
        );

        // 2. 삽입 수행 (시간 측정)
        LogUtils.runAndShowCostLog("테스트 시리즈 저장", () -> seriesRepository.saveAll(seriesList));
    }

    /**
     * builder 기반 User entity 생성
     */
    private User buildUser(long idx, String rawPassword, UserRole role) {
        return User.builder()
                .email("test%06d@test.com".formatted(idx))
                .password(passwordEncoder.encode(rawPassword))
                .name("테스트%06d".formatted(idx))
                .nickname("테스트%06d".formatted(idx))
                .role(role)
                .build();
    }

    /**
     * builder 기반 post entity 생성
     */
    private Post buildPost(User user, PostStatus status, long idx) {
        return Post.builder()
                .user(user)
                .title("제목%06d".formatted(idx))
                .slug("post-%06d".formatted(idx))
                .excerpt("요약%06d".formatted(idx))
                .content("내용%06d".formatted(idx))
                .status(status)
                .build();
    }

    /**
     * builder 기반 series entity 생성
     */
    private Series buildSeries(User user, boolean isPublic, long idx) {
        return Series.builder()
                .user(user)
                .name("시리즈%06d".formatted(idx))
                .slug("series-%06d".formatted(idx))
                .isPublic(isPublic)
                .build();
    }

    /**
     * builder 기반 stack entity 생성
     */
    private Stack buildStack(StackGroup stackGroup, long idx) {
        return Stack.builder()
                .name("스택%06d".formatted(idx))
                .stackGroup(stackGroup)
                .build();
    }



    /**
     * 엔티티 생성 일반화 메소드
     * @param countMethod 현재 개수를 세는 메소드 (ex. userRepository.count())
     * @param mappingMethod Entity 생성 메소드 (ex. User.builder().build())
     * @return Entity
     */
    private <T> T createEntity(
            Supplier<Long> countMethod,
            Function<Long, T> mappingMethod
    ) {
        // 1. 인덱스 계산
        long idx = countMethod.get() + 1;

        // 2. Entity 생성 및 반환
        return mappingMethod.apply(idx);
    }


    /**
     * 엔티티 생성 일반화 메소드
     * @param amount 생성 수량
     * @param countMethod 현재 개수를 세는 메소드 (ex. userRepository.count())
     * @param mappingMethod Entity 생성 메소드 (ex. User.builder().build())
     * @return Entity 리스트
     */
    private <T> List<T> createEntities(
            int amount,
            Supplier<Long> countMethod,
            Function<Long, T> mappingMethod
    ) {
        // 1. 현재 존재 개수 확인
        long currentCount = countMethod.get() + 1;
        long endIdx = currentCount + amount;

        // 2. Entity 생성 및 반환
        return LongStream.range(currentCount, endIdx)
                .mapToObj(mappingMethod::apply)
                .toList();
    }
}
