package com.sealog.backend.support.component;


import com.sealog.backend.domain.feature.archive.entity.Archive;
import com.sealog.backend.domain.feature.archive.entity.ArchivePost;
import com.sealog.backend.domain.feature.archive.repository.ArchivePostRepository;
import com.sealog.backend.domain.feature.archive.repository.ArchiveRepository;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.utils.LogUtils;
import com.sealog.backend.support.constant.TestSql;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
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

    // 의존성
    private final JdbcTemplate jdbcTemplate; // 대용량 삽입
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ArchiveRepository archiveRepository;
    private final ArchivePostRepository archivePostRepository;

    /**
     * TEST User 생성
     * amount = 10 -> 테스트000~테스트009까지 생성
     * @param amount 생성 수량
     * @param role   회원 권한
     */
    public void createTestUsers(int amount, UserRole role) {

        // 1. 사용 패스워드 조회
        String password = passwordEncoder.encode("test");

        // 2. User 생성
        // JPA 방식
        List<User> users = createEntities(
                amount,
                userRepository::count,
                idx -> User.builder()
                        .email("test%06d@test.com".formatted(idx))
                        .password(password)
                        .name("테스트%06d".formatted(idx))
                        .nickname("테스트%06d".formatted(idx))
                        .role(role)
                        .build()
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


        // 3. 삽입 수행 (시간 측정)
        LogUtils.runAndShowCostLog("테스트 사용자 저장", () -> userRepository.saveAll(users));

        // JDBC batch 방식
        //LogUtils.runAndShowCostLog("테스트 사용자 저장", () -> jdbcTemplate.batchUpdate(TestSql.INSERT_USER, users));
    }

    /**
     * TEST User 생성
     * amount = 10 -> 제목000~제목009까지 생성
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
                idx -> Post.builder()
                        .user(user)
                        .title("제목%06d".formatted(idx))
                        .slug("post-%06d".formatted(idx))
                        .excerpt("요약%06d".formatted(idx))
                        .content("내용%06d".formatted(idx))
                        .status(status)
                        .build()
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
     * amount = 10 -> 아카이브000~아카이브009까지 생성
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
                idx -> Archive.builder()
                        .user(user)
                        .name("아카이브%06d".formatted(idx))
                        .slug("archive-%06d".formatted(idx))
                        .isPublic(isPublic)
                        .build()
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
     * TEST ArchivePost 생성
     * @param amount  생성 수량
     * @param archive 소속 아카이브
     * @param post    소속 블로그 게시글
     */
    public void createTestArchivePosts(int amount, Archive archive, Post post) {

        // 1. ArchivePost 생성
        // JPA 방식
        List<ArchivePost> archivePosts = createEntities(
                amount,
                archivePostRepository::count,
                idx -> ArchivePost.builder()
                        .archive(archive)
                        .post(post)
                        .sortOrder(idx.intValue())
                        .build()
        );

        // JDBC 방식
        //List<Object[]> archivePosts = createEntities(
        //        amount,
        //        archivePostRepository::count,
        //        idx -> new Object[] {
        //                archive.getId(),
        //                post.getId(),
        //                idx.intValue()
        //        }
        //);


        // 2. 삽입 수행 (시간 측정)
        // JPA 방식
        LogUtils.runAndShowCostLog("테스트 아카이브 포스트 저장", () -> archivePostRepository.saveAll(archivePosts));

        // JDBC batch 방식
        //LogUtils.runAndShowCostLog("테스트 아카이브 포스트 저장", () -> jdbcTemplate.batchUpdate(TestSql.INSERT_ARCHIVE_POST, archivePosts));
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
