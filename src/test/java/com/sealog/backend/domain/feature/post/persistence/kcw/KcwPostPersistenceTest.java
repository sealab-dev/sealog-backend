package com.sealog.backend.domain.feature.post.persistence.kcw;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
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
 *
 * 비교 대상:
 * - Specification LIKE  : LIKE 기반 풀스캔 검색 (엔티티 반환)
 * - Specification FT    : MATCH AGAINST 기반 FT 인덱스 검색 (엔티티 반환)
 * - QueryDSL FT + DTO   : MATCH AGAINST 기반 FT 인덱스 검색 (DTO 반환, content 컬럼 제외)
 *
 * 성공 기준:
 * ① LIKE > FT : FT 인덱스가 LIKE 풀스캔보다 빠름 (전 케이스 공통)
 *
 * ② FT > QDSL FT+DTO 는 현실적인 매칭 건수 범위에서는 assertThat 대상에서 제외한다.
 * - 이유: 실제 서비스에서 수십만 건 중 수만 건이 한 번에 검색되는 경우는 거의 없음
 * - 수천 건 이하의 매칭 결과는 buffer pool에 충분히 캐시되므로 overflow I/O 차이가 미미
 * - QDSL vs FT 시간 차이는 로그로만 기록하며 참고용으로 관찰한다
 *
 * 키워드 4종 (현실적 검색 시나리오 기반):
 * - 없는 키워드 : 결과 없음  (FT inverted index 즉시 반환, 행 접근 없음)
 * - 소량 키워드 : 결과 극소  (전형적인 희귀 키워드 검색)
 * - 중량 키워드 : 결과 소규모 (일반적인 키워드 검색)
 * - 보편 키워드 : 결과 중규모 (testUser + otherUser 전용 삽입)
 *
 * ⚠️ 보편 키워드는 한글 전용 키워드("보편검색어")로 지정한다.
 *    영어 기술 키워드(Spring 등)는 랜덤 content 생성 시 자연 포함될 수 있어
 *    설계 의도보다 훨씬 많은 행이 매칭될 수 있다.
 *
 * innodb_ft_min_token_size=2 (TestContainer --innodb-ft-min-token-size=2로 보장)
 */
@Slf4j
@DisplayName("Post 영속성 테스트 (PostCondition FULLTEXT 검색)")
class KcwPostPersistenceTest extends PersistenceTest {

    // 사용 의존성
    @Autowired KcwPostRepository postRepository;
    @Autowired TestDataFactory testDataFactory;

    // 검색 키워드 (전용 키워드 배치 방식 — 매칭 건수 정밀 제어)
    // ⚠️ 영어 기술 키워드(Spring 등)는 랜덤 content 생성 시 자연 포함될 수 있으므로 한글 전용 키워드 사용
    private static final String NO_MATCH_KEYWORD = "없는키워드ABC"; // 결과 없음
    private static final String SMALL_KEYWORD    = "희귀검색어";    // 결과 극소
    private static final String MEDIUM_KEYWORD   = "중간검색어";    // 결과 소규모
    private static final String COMMON_KEYWORD   = "보편검색어";    // 결과 중규모 (testUser + otherUser 전용 삽입)

    private static final int CONTENT_LENGTH = 300000;

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

                    // 2. 배경 데이터 (키워드 없음 — LIKE 풀스캔 부하 생성용, ~148K건)
                    LogUtils.showCostLog("QUERY 1", () -> testDataFactory.createTestPosts(100_000, PostStatus.PUBLISHED, testUser));
                    LogUtils.showCostLog("QUERY 2", () -> testDataFactory.createTestPosts(500, PostStatus.PRIVATE,   testUser));
                    LogUtils.showCostLog("QUERY 3", () -> testDataFactory.createTestPosts(500, PostStatus.PUBLISHED, otherUser));
                    LogUtils.showCostLog("QUERY 4", () -> testDataFactory.createDeletedPost(testUser));

                    // 3. 소량 케이스 (극소 매칭)
                    LogUtils.showCostLog("QUERY 5", () -> testDataFactory.createTestPosts(10,  PostStatus.PUBLISHED, testUser, SMALL_KEYWORD,  CONTENT_LENGTH));

                    // 4. 중량 케이스 (소규모 매칭)
                    LogUtils.showCostLog("QUERY 6", () -> testDataFactory.createTestPosts(500, PostStatus.PUBLISHED, testUser, MEDIUM_KEYWORD, CONTENT_LENGTH));

                    // 5. 보편 케이스 (중규모 매칭 — testUser + otherUser 전용 삽입)
                    LogUtils.showCostLog("QUERY 7", () -> testDataFactory.createTestPosts(2_000, PostStatus.PUBLISHED, testUser, COMMON_KEYWORD, CONTENT_LENGTH));
                    LogUtils.showCostLog("QUERY 8", () -> testDataFactory.createTestPosts(500, PostStatus.PUBLISHED, otherUser, COMMON_KEYWORD, CONTENT_LENGTH));

                    // 6. QueryDSL 클래스 로딩 유도
                    // 최초 1번, querydsl 쿼리를 수행하지 않으면, 클래스 로딩으로 인해 실행 시간이 (100ms 이상 지연 발생. 정상 비교 불가)
                    postRepository.searchFt(null, NO_MATCH_KEYWORD, PageRequest.of(0, 1));
                }
        );
    }

    @Test
    @Order(0)
    void warmUp() {
    }

    // =====================================================================
    // Guest (PUBLISHED만, 전체 사용자)
    // 성공 기준 ①: LIKE > FT (전 케이스 공통)
    // ② FT vs QDSL FT+DTO: 로그로만 기록 (현실적 건수 범위에서는 미검증)
    // ⚠️ @Order(1): search_guest가 search_user보다 반드시 먼저 실행되어야 한다.
    //    search_user가 먼저 실행되면 testUser 포스트가 buffer pool에 캐시되어
    //    search_guest의 LIKE 풀스캔이 비정상적으로 빠른 결과를 낼 수 있음.
    // =====================================================================
    @Nested
    @Order(1)
    @DisplayName("Guest — LIKE > FT [① 공통] / FT vs QDSL FT+DTO [참고용 로그] (PUBLISHED, 전체 사용자)")
    class search_guest {

        @Test
        @Order(1)
        @DisplayName("없는 키워드 (결과 없음)")
        void 없는_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, NO_MATCH_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, NO_MATCH_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — Guest 없는 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 없는 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — Guest 없는 키워드", () -> postRepository.searchFt(null, NO_MATCH_KEYWORD, PAGEABLE));

            logCompareResult("Guest 없는 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(2)
        @DisplayName("소량 키워드 (결과 극소)")
        void 소량_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, SMALL_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, SMALL_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — Guest 소량 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 소량 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — Guest 소량 키워드", () -> postRepository.searchFt(null, SMALL_KEYWORD, PAGEABLE));

            logCompareResult("Guest 소량 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(3)
        @DisplayName("중량 키워드 (결과 소규모)")
        void 중량_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, MEDIUM_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, MEDIUM_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — Guest 중량 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 중량 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — Guest 중량 키워드", () -> postRepository.searchFt(null, MEDIUM_KEYWORD, PAGEABLE));

            logCompareResult("Guest 중량 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(4)
        @DisplayName("보편 키워드 (결과 중규모)")
        void 보편_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(null, COMMON_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(null, COMMON_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — Guest 보편 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — Guest 보편 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — Guest 보편 키워드", () -> postRepository.searchFt(null, COMMON_KEYWORD, PAGEABLE));

            logCompareResult("Guest 보편 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }
    }

    // =====================================================================
    // User (전체 상태, testUser만)
    // 성공 기준 ①: LIKE > FT (전 케이스 공통)
    // ② FT vs QDSL FT+DTO: 로그로만 기록 (현실적 건수 범위에서는 미검증)
    // =====================================================================
    @Nested
    @Order(2)
    @DisplayName("User — LIKE > FT [① 공통] / FT vs QDSL FT+DTO [참고용 로그] (전체 상태, testUser만)")
    class search_user {

        @Test
        @Order(5)
        @DisplayName("없는 키워드 (결과 없음)")
        void 없는_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(testUser.getNickname(), NO_MATCH_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(testUser.getNickname(), NO_MATCH_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — User 없는 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — User 없는 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — User 없는 키워드", () -> postRepository.searchFt(testUser.getNickname(), NO_MATCH_KEYWORD, PAGEABLE));

            logCompareResult("User 없는 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(6)
        @DisplayName("소량 키워드 (결과 극소)")
        void 소량_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(testUser.getNickname(), SMALL_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(testUser.getNickname(), SMALL_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — User 소량 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — User 소량 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — User 소량 키워드", () -> postRepository.searchFt(testUser.getNickname(), SMALL_KEYWORD, PAGEABLE));

            logCompareResult("User 소량 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(7)
        @DisplayName("중량 키워드 (결과 소규모)")
        void 중량_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(testUser.getNickname(), MEDIUM_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(testUser.getNickname(), MEDIUM_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — User 중량 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — User 중량 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — User 중량 키워드", () -> postRepository.searchFt(testUser.getNickname(), MEDIUM_KEYWORD, PAGEABLE));

            logCompareResult("User 중량 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }

        @Test
        @Order(8)
        @DisplayName("보편 키워드 (결과 중규모)")
        void 보편_키워드() {
            Specification<Post> ftSpec   = KcwTestPostCondition.search(testUser.getNickname(), COMMON_KEYWORD);
            Specification<Post> likeSpec = KcwTestPostCondition.searchLike(testUser.getNickname(), COMMON_KEYWORD);

            LogUtils.LogResponse<Page<Post>>        like = LogUtils.calculateAndShowCostLog("LIKE — User 보편 키워드", () -> postRepository.findAll(likeSpec, PAGEABLE));
            LogUtils.LogResponse<Page<Post>>        ft   = LogUtils.calculateAndShowCostLog("FT   — User 보편 키워드", () -> postRepository.findAll(ftSpec, PAGEABLE));
            LogUtils.LogResponse<Page<PostSummary>> qdsl = LogUtils.calculateAndShowCostLog("QDSL — User 보편 키워드", () -> postRepository.searchFt(testUser.getNickname(), COMMON_KEYWORD, PAGEABLE));

            logCompareResult("User 보편 키워드", like, ft, qdsl);
            assertSameCount(like, ft, qdsl);
            assertThat(ft.getCost())
                    .as("[①] FT(%dms) < LIKE(%dms) 여야 합니다", ft.getCost(), like.getCost())
                    .isLessThan(like.getCost());
        }
    }


    // =====================================================================
    // 공통 로그 헬퍼
    // =====================================================================

    /**
     * LIKE vs FT vs QDSL FT+DTO 비교 결과 로그 출력
     * 건수는 LIKE 기준 단일 표기 (assertSameCount로 세 방식이 동일함이 보장됨)
     */
    private void logCompareResult(
            String label,
            LogUtils.LogResponse<Page<Post>> like,
            LogUtils.LogResponse<Page<Post>> ft,
            LogUtils.LogResponse<Page<PostSummary>> qdsl
    ) {
        log.warn("[{} - {}] LIKE={}ms | FT={}ms | QDSL FT+DTO={}ms",
                label,
                "%,d건".formatted(like.getResult().getTotalElements()),
                like.getCost(),
                ft.getCost(),
                qdsl.getCost());
    }

    /**
     * 세 검색 방식의 결과 건수 일치 검증 — 잘못된 비교 방지
     * LIKE 결과 건수를 기준으로 FT, QDSL FT+DTO가 동일한지 확인
     */
    private void assertSameCount(
            LogUtils.LogResponse<Page<Post>> like,
            LogUtils.LogResponse<Page<Post>> ft,
            LogUtils.LogResponse<Page<PostSummary>> qdsl
    ) {
        long likeCount = like.getResult().getTotalElements();
        long ftCount   = ft.getResult().getTotalElements();
        long qdslCount = qdsl.getResult().getTotalElements();

        assertThat(ftCount)
                .as("[건수 일치] FT(%,d건) == LIKE(%,d건) 여야 합니다", ftCount, likeCount)
                .isEqualTo(likeCount);
        assertThat(qdslCount)
                .as("[건수 일치] QDSL FT+DTO(%,d건) == LIKE(%,d건) 여야 합니다", qdslCount, likeCount)
                .isEqualTo(likeCount);
    }
}
