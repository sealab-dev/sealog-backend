package com.sealog.backend.domain.feature.post.persistence;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.repository.kcw.KcwTestPostCondition;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.global.utils.LogUtils;
import com.sealog.backend.support.base.test.PersistenceTest;
import com.sealog.backend.support.component.TestDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 데이터베이스 성능 개선을 위한 영속성 테스트 클래스
 */
@Slf4j
@DisplayName("Post 영속성 테스트 (PostCondition FULLTEXT 검색)")
class KcwPostPersistenceTest extends PersistenceTest {

    // 사용 의존성
    @Autowired PostRepository postRepository;
    @Autowired TestDataFactory testDataFactory;

    // 검색 키워드
    private static final String NO_MATCH_KEYWORD     = "없는키워드ABC"; // → 0건
    private static final String RARE_KEYWORD         = "희귀검색어";    // → ~5건
    private static final String MATCH_KEYWORD_TECH   = "Spring";       // TECH 1/16 → PUBLISHED ~10,625건
    private static final String MATCH_KEYWORD_SUFFIX = "가이드";        // SUFFIX 1/10 → PUBLISHED ~17,000건

    private static final int RARE_POST_COUNT = 5;
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    private User testUser;
    private User otherUser;

    @BeforeAll
    void setUp() {

        LogUtils.showCostLog(
                "PostCondition 영속성 테스트 필요 데이터 초기화", () -> {

                    // 1. 사용자 생성
                    testUser  = testDataFactory.createUser(UserRole.USER);
                    otherUser = testDataFactory.createUser(UserRole.USER);

                    // 2. 게시글 대량 생성
                    LogUtils.showCostLog("QUERY 1", () -> testDataFactory.createTestPosts(150_000, PostStatus.PUBLISHED, testUser));
                    LogUtils.showCostLog("QUERY 2", () -> testDataFactory.createTestPosts(30_000, PostStatus.PRIVATE, testUser));
                    LogUtils.showCostLog("QUERY 3", () -> testDataFactory.createTestPosts(20_000, PostStatus.PUBLISHED, otherUser));
                    LogUtils.showCostLog("QUERY 4", () -> testDataFactory.createDeletedPost(testUser));

                    // 3. 소량 결과 케이스용 특수 키워드 게시글 (RARE_KEYWORD, PUBLISHED)
                    LogUtils.showCostLog("QUERY 5", () -> {
                        for (int i = 0; i < RARE_POST_COUNT; i++) {
                            testDataFactory.createPost(testUser, PostStatus.PUBLISHED, RARE_KEYWORD + " 테스트 " + i);
                        }
                    });
                }
        );
    }

    @Test
    @Order(0)
    void warmUp() {
        // 아무것도 안 함, JVM 웜업용
    }

    // =====================================================================
    // search() vs searchLike() — Guest (PUBLISHED만, 전체 사용자)
    // 대상 데이터: PUBLISHED 170,005건 (testUser 150,005 + otherUser 20,000)
    // 성공 기준: FT 소요 시간 < LIKE 소요 시간
    // =====================================================================
    @Nested
    @DisplayName("search() vs searchLike() — Guest (PUBLISHED, 전체 사용자)")
    class search_guest {

        @Test
        @Order(1)
        @DisplayName("0건 — FT가 LIKE보다 빠름")
        void 미매칭_0건() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, NO_MATCH_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, NO_MATCH_KEYWORD);

            LogUtils.LogResponse<Page<Post>> ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 0건 (" + NO_MATCH_KEYWORD + ")", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>> like = LogUtils.calculateAndShowCostLog("LIKE — Guest 0건 (" + NO_MATCH_KEYWORD + ")", () -> postRepository.findAll(likeSpec, PAGEABLE));

            logCompareResult("Guest 0건", ft, like);
            assertThat(ft.getCost())
                    .as("FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(2)
        @DisplayName("소량 (~5건) — FT가 LIKE보다 빠름")
        void 소량_검색() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, RARE_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, RARE_KEYWORD);

            LogUtils.LogResponse<Page<Post>> ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 소량 (~" + RARE_POST_COUNT + "건, " + RARE_KEYWORD + ")", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>> like = LogUtils.calculateAndShowCostLog("LIKE — Guest 소량 (~" + RARE_POST_COUNT + "건, " + RARE_KEYWORD + ")", () -> postRepository.findAll(likeSpec, PAGEABLE));

            logCompareResult("Guest 소량", ft, like);
            assertThat(ft.getCost())
                    .as("FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(3)
        @DisplayName("중간 (~10,625건) — FT가 LIKE보다 빠름")
        void 중간_검색() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, MATCH_KEYWORD_TECH);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, MATCH_KEYWORD_TECH);

            LogUtils.LogResponse<Page<Post>> ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 중간 (Spring, PUBLISHED 170k×1/16≈10,625건)", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>> like = LogUtils.calculateAndShowCostLog("LIKE — Guest 중간 (Spring, PUBLISHED 170k×1/16≈10,625건)", () -> postRepository.findAll(likeSpec, PAGEABLE));

            logCompareResult("Guest 중간", ft, like);
            assertThat(ft.getCost())
                    .as("FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(4)
        @DisplayName("대량 (~17,001건) — FT가 LIKE보다 빠름")
        void 대량_검색() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, MATCH_KEYWORD_SUFFIX);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, MATCH_KEYWORD_SUFFIX);

            LogUtils.LogResponse<Page<Post>> ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 대량 (가이드, PUBLISHED 170k×1/10≈17,001건)", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>> like = LogUtils.calculateAndShowCostLog("LIKE — Guest 대량 (가이드, PUBLISHED 170k×1/10≈17,001건)", () -> postRepository.findAll(likeSpec, PAGEABLE));

            logCompareResult("Guest 대량", ft, like);
            assertThat(ft.getCost())
                    .as("FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }
    }

    // =====================================================================
    // search() vs searchLike() — User (전체 상태, 닉네임 일치 사용자만)
    // 대상 데이터: testUser 전체 180,005건 (PUBLISHED 150,005 + PRIVATE 30,000)
    // 성공 기준: FT 소요 시간 < LIKE 소요 시간
    // =====================================================================
    @Nested
    @DisplayName("search() vs searchLike() — User (전체 상태, testUser만)")
    class search_user {

        @Test
        @Order(5)
        @DisplayName("0건 — FT가 LIKE보다 빠름")
        void 미매칭_0건() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(testUser.getNickname(), NO_MATCH_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(testUser.getNickname(), NO_MATCH_KEYWORD);

            LogUtils.LogResponse<Page<Post>> ft   = LogUtils.calculateAndShowCostLog("FT   — User 0건 (" + NO_MATCH_KEYWORD + ")", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>> like = LogUtils.calculateAndShowCostLog("LIKE — User 0건 (" + NO_MATCH_KEYWORD + ")", () -> postRepository.findAll(likeSpec, PAGEABLE));

            logCompareResult("User 0건", ft, like);
            assertThat(ft.getCost())
                    .as("FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(6)
        @DisplayName("소량 (~5건) — FT가 LIKE보다 빠름")
        void 소량_검색() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(testUser.getNickname(), RARE_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(testUser.getNickname(), RARE_KEYWORD);

            LogUtils.LogResponse<Page<Post>> ft   = LogUtils.calculateAndShowCostLog("FT   — User 소량 (~" + RARE_POST_COUNT + "건, " + RARE_KEYWORD + ")", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>> like = LogUtils.calculateAndShowCostLog("LIKE — User 소량 (~" + RARE_POST_COUNT + "건, " + RARE_KEYWORD + ")", () -> postRepository.findAll(likeSpec, PAGEABLE));

            logCompareResult("User 소량", ft, like);
            assertThat(ft.getCost())
                    .as("FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(7)
        @DisplayName("대량 (~11,250건) — FT가 LIKE보다 빠름")
        void 대량_검색() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(testUser.getNickname(), MATCH_KEYWORD_TECH);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(testUser.getNickname(), MATCH_KEYWORD_TECH);

            LogUtils.LogResponse<Page<Post>> ft   = LogUtils.calculateAndShowCostLog("FT   — User 대량 (Spring, testUser 180k×1/16≈11,250건)", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>> like = LogUtils.calculateAndShowCostLog("LIKE — User 대량 (Spring, testUser 180k×1/16≈11,250건)", () -> postRepository.findAll(likeSpec, PAGEABLE));

            logCompareResult("User 대량", ft, like);
            assertThat(ft.getCost())
                    .as("FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }
    }


    // =====================================================================
    // 공통 로그 헬퍼
    // =====================================================================

    /**
     * FT vs LIKE 비교 결과 로그 출력
     */
    private void logCompareResult(String label, LogUtils.LogResponse<Page<Post>> ft, LogUtils.LogResponse<Page<Post>> like) {
        log.warn("[{}] FT={}ms vs LIKE={}ms | FT 결과={}, LIKE 결과={}",
                label,
                ft.getCost(), like.getCost(),
                "%,d개".formatted(ft.getResult().getTotalElements()),
                "%,d개".formatted(like.getResult().getTotalElements()));
    }

    /**
     * 단일 검색 결과 로그 출력
     */
    private void logResult(String label, LogUtils.LogResponse<Page<Post>> response) {
        String totalElements       = "%,d개".formatted(response.getResult().getTotalElements());
        String currentPageElements = "%,d개".formatted(response.getResult().getNumberOfElements());
        String msCost              = "%,dms".formatted(response.getCost());
        log.warn("[{}] 총 결과 = {}, 현재 페이지 = {}, 소요 시간 = {}", label, totalElements, currentPageElements, msCost);
    }
}
