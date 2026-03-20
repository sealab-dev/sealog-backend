package com.sealog.backend.support.component;


import com.sealog.backend.domain.base.util.SlugUtils;
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
import com.sealog.backend.support.base.container.TestContainer;
import com.sealog.backend.support.constant.TestSql;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * 테스트 데이터 생성을 위한 클래스
 */

@Transactional
@TestComponent // 운영 환경에서 생성 방지
@RequiredArgsConstructor
public class TestDataFactory {

    // repository 의존성
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final StackRepository stackRepository;

    // JDBC 직접 사용
    private final JdbcTemplate jdbcTemplate;

    // 패스워드 인코더 직접 주입(스프링 의존성 제거)
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();



    // =========================================================
    // 태이블 일괄 삭제
    // =========================================================

    /**
     * 테스트 환경의 테이블 내 데이터 일괄 삭제 (TRUNCATE)
     * DELETE 기반 삭제보다 빠르고, AUTO_INCREMENT 초기화
     */
    public void clearTable() {

        // 1. FK Constraint 비활성화
        jdbcTemplate.execute(TestSql.FOREIGN_KEY_CHECKS_INACTIVATION);

        // 2. 테이블 행 일괄 삭제
        jdbcTemplate
                .queryForList(TestSql.SELECT_TABLE_NAMES, String.class, TestContainer.DEFAULT_DATABASE_NAME)
                .forEach(tableName -> jdbcTemplate.execute(TestSql.TRUNCATE_TABLE + tableName));

        // 3. FK Constraint 활성화
        jdbcTemplate.execute(TestSql.FOREIGN_KEY_CHECKS_ACTIVATION);
    }


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
                createEntity(postRepository::count, idx -> buildPost(idx, user, status))
        );
    }


    /**
     * Post 단일 생성 (커스텀 제목)
     * @param user   작성자
     * @param status 게시글 상태
     * @param title  커스텀 제목
     * @return 저장된 Post 엔티티 (ID 포함)
     */
    public Post createPost(User user, PostStatus status, String title) {
        return postRepository.save(
                createEntity(postRepository::count, idx -> buildPost(idx, user, status, title))
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
        Post post = createEntity(postRepository::count, idx -> buildPost(idx, user, status));
        post.addToSeries(series);

        // 2. 생성 및 반환
        return postRepository.save(post);
    }

    /**
     * Post 단일 생성 (소프트 삭제 상태)
     * @param user 작성자
     * @return deletedAt이 설정된 소프트 삭제된 Post 엔티티 (ID 포함)
     */
    public Post createDeletedPost(User user) {

        // 1. 엔티티 생성
        Post post = createEntity(
                postRepository::count, idx -> buildPost(idx, user, PostStatus.PUBLISHED)
        );

        // 2. 소프트 삭제 처리
        post.softDelete();

        // 3. 저장 및 반환
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
                createEntity(seriesRepository::count, idx -> buildSeries(idx, user, isPublic))
        );
    }


    /**
     * Stack 단일 생성
     * @param stackGroup 스택 그룹
     * @return 저장된 Stack 엔티티 (ID 포함)
     */
    public Stack createStack(StackGroup stackGroup) {
        return stackRepository.save(
                createEntity(stackRepository::count, idx -> buildStack(idx, stackGroup))
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
        List<Object[]> jdbcUsers = createEntities(
                amount, userRepository::count, idx -> createUserArray(idx, "password", role)
        );

        // 2. 삽입 수행 (시간 측정)
        processBatchQuery(jdbcUsers, batch -> jdbcTemplate.batchUpdate(TestSql.INSERT_USER, batch));
    }

    /**
     * TEST Post 생성
     * @param amount   생성 수량
     * @param status   블로그 게시글 상태
     * @param user     블로그 작성자 회원 엔티티
     */
    public void createTestPosts(int amount, PostStatus status, User user) {

        createAndInsertEntities(
                amount,
                postRepository::count,
                idx -> createPostArray(idx, status, user, 0),
                chunk -> processBatchQuery(chunk, batch -> jdbcTemplate.batchUpdate(TestSql.INSERT_POST, batch))
        );
    }

    /**
     * TEST Post 생성 (본문 길이 지정)
     * @param amount        생성 수량
     * @param status        블로그 게시글 상태
     * @param user          블로그 작성자 회원 엔티티
     * @param contentLength content 필드 길이 (글자 수)
     */
    public void createTestPosts(int amount, PostStatus status, User user, int contentLength) {

        createAndInsertEntities(
                amount,
                postRepository::count,
                idx -> createPostArray(idx, status, user, contentLength),
                chunk -> processBatchQuery(chunk, batch -> jdbcTemplate.batchUpdate(TestSql.INSERT_POST, batch))
        );
    }

    /**
     * TEST Post 생성 (모든 게시글 제목에 지정 키워드 포함)
     *
     * @param amount   생성 수량
     * @param status   블로그 게시글 상태
     * @param user     블로그 작성자 회원 엔티티
     * @param title    블로그 제목
     */
    public void createTestPosts(int amount, PostStatus status, User user, String title, int contentLength) {

        createAndInsertEntities(
                amount,
                postRepository::count,
                idx -> createPostArray(idx, status, user, title, contentLength),
                chunk -> processBatchQuery(chunk, batch -> jdbcTemplate.batchUpdate(TestSql.INSERT_POST, batch))
        );
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
        List<Object[]> jdbcSeries = createEntities(
                amount, seriesRepository::count, idx -> createSeriesArray(idx, user, isPublic)
        );

        // 2. 삽입 수행 (시간 측정)
        processBatchQuery(jdbcSeries, batch -> jdbcTemplate.batchUpdate(TestSql.INSERT_SERIES, batch));
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

    private Object[] createUserArray(long idx, String rawPassword, UserRole role) {
        return new Object[]{
                "test%06d@test.com".formatted(idx),
                passwordEncoder.encode(rawPassword),
                "테스트%06d".formatted(idx),
                "테스트%06d".formatted(idx),
                role.name()
        };
    }

    /**
     * builder 기반 post entity 생성
     */
    private Post buildPost(long idx, User user, PostStatus status) {

        String title = TestTextGenerator.generatePostTitle(idx);
        return Post.builder()
                .user(user)
                .title(title)
                .slug(SlugUtils.generate(title))
                .excerpt("요약%06d".formatted(idx))
                .content("내용%06d".formatted(idx))
                .status(status)
                .build();
    }

    private Post buildPost(long idx, User user, PostStatus status, String title) {
        return Post.builder()
                .user(user)
                .title(title)
                .slug(SlugUtils.generate(title))
                .excerpt("요약%06d".formatted(idx))
                .content("내용%06d".formatted(idx))
                .status(status)
                .build();
    }

    private Object[] createPostArray(long idx, PostStatus status, User user, int contentLength) {

        // 1. 테스트 제목, 본문
        String title = TestTextGenerator.generatePostTitle(idx);
        String content = contentLength > 0 ?
                TestTextGenerator.generatePostContent(contentLength) :
                "테스트본문%06d".formatted(idx);

        // 2. Post 엔티티 배열 생성
        return new Object[]{
                user.getId(),
                title,
                SlugUtils.generate(title),
                "요약%06d".formatted(idx),
                content,
                status.name()
        };
    }

    private Object[] createPostArray(long idx, PostStatus status, User user, String title, int contentLength) {

        // 1. 테스트 본문
        String formattedTitle = "%s%06d".formatted(title, idx);
        String content = contentLength > 0 ?
                TestTextGenerator.generatePostContent(contentLength) :
                "테스트본문%06d".formatted(idx);

        return new Object[]{
                user.getId(),
                formattedTitle,
                SlugUtils.generate(formattedTitle),
                "요약%06d".formatted(idx),
                content,
                status.name()
        };
    }


    /**
     * builder 기반 series entity 생성
     */
    private Series buildSeries(long idx, User user, boolean isPublic) {

        String name = "시리즈%06d".formatted(idx);
        return Series.builder()
                .user(user)
                .name(name)
                .slug(SlugUtils.generate(name))
                .isPublic(isPublic)
                .build();
    }

    private Object[] createSeriesArray(long idx, User user, boolean isPublic) {

        String name = "시리즈%06d".formatted(idx);
        return new Object[]{
                user.getId(),
                name,
                SlugUtils.generate(name),
                isPublic
        };
    }


    /**
     * builder 기반 stack entity 생성
     */
    private Stack buildStack(long idx, StackGroup stackGroup) {
        return Stack.builder()
                .name("스택%06d".formatted(idx))
                .stackGroup(stackGroup)
                .build();
    }



    // =========================================================
    // 배치 처리
    // =========================================================

    private static final int BATCH_SIZE = 1_000;

    /**
     * 대용량 파라미터 리스트를 BATCH_SIZE 단위로 분할하여 배치 작업 실행 (JDBC 전용)
     */
    private void processBatchQuery(List<Object[]> params, Consumer<List<Object[]>> batchAction) {
        for (int i = 0; i < params.size(); i += BATCH_SIZE)
            batchAction.accept(params.subList(i, Math.min(i + BATCH_SIZE, params.size())));
    }

    // =========================================================
    // 엔티티 생성 헬퍼
    // =========================================================

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


    private void createAndInsertEntities(
            int amount,
            Supplier<Long> countMethod,
            Function<Long, Object[]> mappingMethod,
            Consumer<List<Object[]>> batchAction
    ) {
        long currentCount = countMethod.get() + 1;
        long endIdx = currentCount + amount;

        List<Object[]> chunk = new ArrayList<>(BATCH_SIZE);

        for (long i = currentCount; i < endIdx; i++) {
            chunk.add(mappingMethod.apply(i));

            if (chunk.size() == BATCH_SIZE) {
                batchAction.accept(chunk);
                chunk.clear(); // 메모리 즉시 해제
            }
        }

        // 나머지 처리
        if (!chunk.isEmpty()) {
            batchAction.accept(chunk);
        }
    }
}
