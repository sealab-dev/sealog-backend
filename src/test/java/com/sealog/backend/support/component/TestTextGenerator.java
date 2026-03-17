package com.sealog.backend.support.component;

/**
 * 영속성 테스트 전용 텍스트 생성 유틸리티
 *
 * 특징:
 * - idx 기반 결정적(deterministic) 생성 — 동일 idx는 항상 동일 텍스트 반환
 * - 시드 없이 재현 가능 (Random 미사용)
 * - 단어 조합 방식: TECH × TOPIC × SUFFIX = 16 × 16 × 10 = 2,560가지 조합
 * - 2,560 초과 시 사이클 번호를 suffix로 추가하여 완전 고유 제목 보장
 */
public final class TestTextGenerator {

    private TestTextGenerator() {}

    // =========================================================
    // 단어 상수
    // =========================================================

    private static final String[] TECH = {
            "Spring", "React", "Docker", "AWS", "Java", "Redis", "Kafka", "Nginx",
            "Kubernetes", "Python", "TypeScript", "Next.js", "GraphQL", "PostgreSQL", "MongoDB", "Linux"
    };  // 16개

    private static final String[] TOPIC = {
            "배포", "설정", "최적화", "트러블슈팅", "입문", "비교", "실전", "정리",
            "아키텍처", "보안", "모니터링", "마이그레이션", "테스트", "CI/CD", "성능", "운영"
    };  // 16개

    private static final String[] SUFFIX = {
            "가이드", "후기", "팁", "방법", "노트", "정리", "사례", "분석", "실습", "완전정복"
    };  // 10개

    private static final int TECH_LEN   = TECH.length;
    private static final int TOPIC_LEN  = TOPIC.length;
    private static final int SUFFIX_LEN = SUFFIX.length;
    private static final int TOTAL      = TECH_LEN * TOPIC_LEN * SUFFIX_LEN;

    // 클래스 로딩 시 문자열 1회만 생성
    private static final String[] BASE_TITLES;

    static {
        BASE_TITLES = new String[TOTAL];
        for (int i = 0; i < TOTAL; i++) {
            BASE_TITLES[i] = TECH[i % TECH_LEN]
                    + " " + TOPIC[(i / TECH_LEN) % TOPIC_LEN]
                    + " " + SUFFIX[(i / (TECH_LEN * TOPIC_LEN)) % SUFFIX_LEN];
        }
    }


    // =========================================================
    // 제목 생성
    // =========================================================

    /**
     * idx 기반 결정적 제목 생성
     *
     * 동작 방식:
     * - 16 × 16 × 10 = 2,560 조합을 순환하며 제목 생성
     * - idx가 2,560을 초과하면 사이클 번호(1, 2, 3...)를 suffix로 추가하여 완전 고유 제목 보장
     *
     * 예시:
     * - idx=0    → "Spring 배포 가이드"
     * - idx=1    → "React 배포 가이드"
     * - idx=16   → "Spring 설정 가이드"
     * - idx=2559 → "Linux CI/CD 완전정복"
     * - idx=2560 → "Spring 배포 가이드 1"   (2번째 사이클 시작)
     * - idx=5120 → "Spring 배포 가이드 2"   (3번째 사이클 시작)
     *
     * @param idx 인덱스 (0 이상)
     * @return 조합된 제목 문자열
     */
    public static String generatePostTitle(long idx) {
        int baseIdx = (int)(idx % TOTAL);
        long cycle  = idx / TOTAL;
        return cycle == 0
                ? BASE_TITLES[baseIdx]              // 배열 조회만 (나노초 단위)
                : BASE_TITLES[baseIdx] + " " + cycle; // 배열 조회 + 단순 concat
    }

    /**
     * 특정 키워드를 frequency 간격마다 포함하는 제목 생성
     *
     * 예시:
     * - generatePostTitle(0,    "검색테스트", 1000) → "검색테스트 Spring 배포 가이드"  (0 % 1000 == 0)
     * - generatePostTitle(1,    "검색테스트", 1000) → "React 배포 가이드"              (1 % 1000 != 0)
     * - generatePostTitle(1000, "검색테스트", 1000) → "검색테스트 Spring 배포 후기 …"  (1000 % 1000 == 0)
     *
     * @param idx           인덱스 (0 이상)
     * @param targetKeyword frequency 간격마다 삽입할 키워드
     * @param frequency     키워드 삽입 간격 (예: 1000이면 1000건 중 1건 포함)
     * @return 조합된 제목 문자열
     */
    public static String generatePostTitle(long idx, String targetKeyword, long frequency) {
        if (idx % frequency == 0) {
            return targetKeyword + " " + generatePostTitle(idx);
        }
        return generatePostTitle(idx);
    }
}
