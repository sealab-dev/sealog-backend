package com.sealog.backend.domain.feature.post.persistence.kcw;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.sealog.backend.domain.feature.post.entity.QPost;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.infra.orm.constant.HibernateFunction;
import com.sealog.backend.infra.orm.utils.QuerydslUtils;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * Post QueryDSL 검색 조건 모음 (kcw 실험용)
 *
 * 사용 목적:
 * - BooleanExpression 기반 동적 조건 조합
 * - nickname = null  → Guest (PUBLISHED만, 전체 사용자)
 * - nickname != null → User (전체 상태, 닉네임 일치 사용자만)
 *
 * 검색 방식:
 * - search / searchByNickname     → LIKE %keyword% 기반 (비교용)
 * - searchFt / searchByNicknameFt → MATCH AGAINST 기반 FT 인덱스 활용
 */
@UtilityClass
public class KcwPostQueryCondition {

    // ========== LIKE 기반 (비교용) ========== //

    /**
     * 게시글 검색 조건 조합 — LIKE 기반
     *
     * @param nickname 요청자 닉네임 (null = Guest, non-null = User)
     * @param keyword  검색 키워드
     * @return 조합된 BooleanExpression
     */
    public static BooleanExpression search(String nickname, String keyword) {
        return notDeleted()
                .and(keywordContains(keyword))
                .and(onlyPublishedIfNicknameAbsent(nickname))
                .and(nicknameEquals(nickname));
    }

    /**
     * 특정 사용자의 공개 게시글 검색 조건 조합 — LIKE 기반
     *
     * @param nickname 검색 대상 사용자 닉네임
     * @param keyword  검색 키워드
     * @return 조합된 BooleanExpression
     */
    public static BooleanExpression searchByNickname(String nickname, String keyword) {
        return notDeleted()
                .and(keywordContains(keyword))
                .and(onlyPublished())
                .and(nicknameEquals(nickname));
    }

    // ========== FT 인덱스 기반 ========== //

    /**
     * 게시글 검색 조건 조합 — FULLTEXT 인덱스 기반
     * - MATCH(title, excerpt) AGAINST (keyword* IN BOOLEAN MODE)
     *
     * @param nickname 요청자 닉네임 (null = Guest, non-null = User)
     * @param keyword  검색 키워드
     * @return 조합된 BooleanExpression
     */
    public static BooleanExpression searchFt(String nickname, String keyword) {
        return notDeleted()
                .and(keywordContainsFt(keyword))
                .and(onlyPublishedIfNicknameAbsent(nickname))
                .and(nicknameEquals(nickname));
    }

    /**
     * 특정 사용자의 공개 게시글 검색 조건 조합 — FULLTEXT 인덱스 기반
     *
     * @param nickname 검색 대상 사용자 닉네임
     * @param keyword  검색 키워드
     * @return 조합된 BooleanExpression
     */
    public static BooleanExpression searchByNicknameFt(String nickname, String keyword) {
        return notDeleted()
                .and(keywordContainsFt(keyword))
                .and(onlyPublished())
                .and(nicknameEquals(nickname));
    }

    // ========== private conditions ========== //

    private static BooleanExpression notDeleted() {
        return QPost.post.deletedAt.isNull();
    }

    /**
     * LIKE %keyword% 기반 키워드 검색 (풀스캔, 비교용)
     */
    private static BooleanExpression keywordContains(String keyword) {
        return QPost.post.title.contains(keyword)
                .or(QPost.post.excerpt.contains(keyword));
    }

    /**
     * MATCH AGAINST 기반 키워드 검색 (FT 인덱스 활용)
     * - Hibernate에 등록된 match_against 함수를 QueryDSL booleanTemplate으로 호출
     */
    private static BooleanExpression keywordContainsFt(String keyword) {
        return QuerydslUtils.createScoreBooleanExp(HibernateFunction.MATCH_AGAINST, QPost.post.title, QPost.post.excerpt, keyword);
    }

    private static BooleanExpression onlyPublishedIfNicknameAbsent(String nickname) {
        return Objects.isNull(nickname) ? QPost.post.status.eq(PostStatus.PUBLISHED) : null;
    }

    private static BooleanExpression onlyPublished() {
        return QPost.post.status.eq(PostStatus.PUBLISHED);
    }

    private static BooleanExpression nicknameEquals(String nickname) {
        return Objects.nonNull(nickname) ? QPost.post.user.nickname.eq(nickname) : null;
    }
}
