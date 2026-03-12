package com.sealog.backend.support.component;


import com.sealog.backend.domain.feature.archive.entity.Archive;
import com.sealog.backend.domain.feature.archive.repository.ArchiveRepository;
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
    private final ArchiveRepository archiveRepository;
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
     * @param archive 게시글 소속 아카이브
     * @param status  게시글 상태
     * @return 저장된 Post 엔티티 (ID 포함)
     */
    public Post createPost(User user, Archive archive, PostStatus status) {

        // 1. 엔티티 생성 및 아카이브 삽입
        Post post = createEntity(postRepository::count, idx -> buildPost(user, status, idx));
        post.addToArchive(archive);

        // 2. 생성 및 반환
        return postRepository.save(post);
    }

    /**
     * Archive 단일 생성
     * @param user     소유자
     * @param isPublic 공개 여부
     * @return 저장된 Archive 엔티티 (ID 포함)
     */
    public Archive createArchive(User user, boolean isPublic) {
        return archiveRepository.save(
                createEntity(archiveRepository::count, idx -> buildArchive(user, isPublic, idx))
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

    /*
     * todo : [대용량 데이터 생성 방식 비교: JPA vs JDBC batchUpdate]
     * JPA saveAll()
     * - 엔티티를 영속성 컨텍스트에 등록하고 스냅샷(변경 감지용 원본 복사본)을 생성
     * - batch_size 설정으로 묶어서 전송하더라도 영속성 컨텍스트에 엔티티가 쌓이는 구조
     * - 대용량 데이터 생성 시 메모리 부하 발생 → 대용량에 부적합
     *
     * JDBC batchUpdate()
     * - 영속성 컨텍스트 없이 SQL + 파라미터 배열을 DB에 직접 전송
     * - 스냅샷 생성 없음 → 메모리 부하 없음
     * - 대용량 데이터 생성에 적합
     *
     * 결론
     * - 수백건 이하 → JPA saveAll() 충분
     * - 수만건 이상 → JDBC batchUpdate() 권장
     * - 두 방식을 비교해보는 것은 의미가 있을 듯
     */

    /**
     * TEST User 생성
     * amount = 10 -> 테스트000000~테스트000009까지 생성
     * @param amount 생성 수량
     * @param role   회원 권한
     */
    public void createTestUsers(int amount, UserRole role) {

        // 1. User 생성
        // JPA 방식
        List<User> users = createEntities(
                amount,
                userRepository::count,
                idx -> buildUser(idx, "password", role)
        );

        // jdbc bulk 연산 방식
        //List<Object[]> users = createEntities(
        //        amount,
        //        userRepository::count,
        //        idx -> new Object[] {
        //                "test%06d@test.com".formatted(idx),
        //                password,
        //                "테스트%06d".formatted(idx),
        //                "테스트%06d".formatted(idx),
        //                role.name()
        //        }
        //);


        // 2. 삽입 수행 (시간 측정)
        LogUtils.runAndShowCostLog("테스트 사용자 저장", () -> userRepository.saveAll(users));

        // JDBC batch 방식
        //LogUtils.runAndShowCostLog("테스트 사용자 저장", () -> jdbcTemplate.batchUpdate(TestSql.INSERT_USER, users));
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
        // JPA 방식
        List<Post> posts = createEntities(
                amount,
                postRepository::count,
                idx -> buildPost(user, status, idx)
        );

        // JDBC 방식
//        List<Object[]> posts = createEntities(
//                amount,
//                postRepository::count,
//                idx -> new Object[]{
//                        user.getId(),
//                        "제목%06d".formatted(idx),
//                        "post-%06d".formatted(idx),
//                        "요약%06d".formatted(idx),
//                        "내용%06d".formatted(idx),
//                        status.name()
//                }
//        );

        // 2. 삽입 수행 (시간 측정)
        // JPA 방식
        LogUtils.runAndShowCostLog("테스트 블로그 저장", () -> postRepository.saveAll(posts));

        // JDBC batch 방식
        //LogUtils.runAndShowCostLog("테스트 블로그 저장", () -> jdbcTemplate.batchUpdate(TestSql.INSERT_POST, posts));
    }


    /**
     * TEST Archive 생성
     * amount = 10 -> 아카이브000000~아카이브000009까지 생성
     * @param amount    생성 수량
     * @param isPublic  공개 여부
     * @param user      아카이브 생성 사용자 엔티티
     */
    public void createTestArchives(int amount, boolean isPublic, User user) {

        // 1. Archive 생성
        // JPA 방식
        List<Archive> archives = createEntities(
                amount,
                archiveRepository::count,
                idx -> buildArchive(user, isPublic, idx)
        );

        // JDBC 방식
//        List<Object[]> archives = createEntities(
//                amount,
//                archiveRepository::count,
//                idx -> new Object[] {
//                        user.getId(),
//                        "아카이브%06d".formatted(idx),
//                        "archive-%06d".formatted(idx),
//                        isPublic
//                }
//        );

        // 2. 삽입 수행 (시간 측정)
        // JPA 방식
        LogUtils.runAndShowCostLog("테스트 아카이브 저장", () -> archiveRepository.saveAll(archives));

        // JDBC batch 방식
        //LogUtils.runAndShowCostLog("테스트 아카이브 저장", () -> jdbcTemplate.batchUpdate(TestSql.INSERT_ARCHIVE, archives));
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
     * builder 기반 archive entity 생성
     */
    private Archive buildArchive(User user, boolean isPublic, long idx) {
        return Archive.builder()
                .user(user)
                .name("아카이브%06d".formatted(idx))
                .slug("archive-%06d".formatted(idx))
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