package com.sealog.backend.domain.feature.post.repository.condition;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

/**
 * Post 검색 조건 Specification 모음
 *
 * 사용 목적:
 * - Guest/User 역할별 동적 조건 조합
 * - nickname = null  → Guest (PUBLISHED만, 전체 사용자)
 * - nickname != null → User (전체 상태, 닉네임 일치 사용자만)
 * - searchByNickname → 특정 사용자의 PUBLISHED 게시글만 (공개 검색)
 */
@UtilityClass
public class PostCondition {

    /**
     * 게시글 검색 조건 조합
     * - 삭제되지 않은 게시글 + 키워드 매칭 + 역할별 조건
     *
     * @param nickname 요청자 닉네임 (null = Guest, non-null = User)
     * @param keyword  검색 키워드
     * @return 조합된 Specification
     */
    public static Specification<Post> search(String nickname, String keyword) {
        return Specification.allOf(
                notDeleted(),
                keywordContains(keyword),
                onlyPublishedIfNicknameAbsent(nickname),
                nicknameEquals(nickname)
        );
    }

    /**
     * 특정 사용자의 공개 게시글 검색 조건 조합
     * - 삭제되지 않은 게시글 + PUBLISHED + 닉네임 일치 + 키워드 매칭
     *
     * @param nickname 검색 대상 사용자 닉네임
     * @param keyword  검색 키워드
     * @return 조합된 Specification
     */
    public static Specification<Post> searchByNickname(String nickname, String keyword) {
        return Specification.allOf(
                notDeleted(),
                keywordContains(keyword),
                onlyPublished(),
                nicknameEquals(nickname)
        );
    }

    // ========== private conditions ========== //

    /**
     * 소프트 삭제되지 않은 게시글
     */
    private static Specification<Post> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    /**
     * 제목 또는 요약에 키워드 포함
     */
    private static Specification<Post> keywordContains(String keyword) {
        return (root, query, cb) -> cb.or(
                cb.like(root.get("title"), "%" + keyword + "%"),
                cb.like(root.get("excerpt"), "%" + keyword + "%")
        );
    }

    /**
     * nickname이 없는 경우(Guest) PUBLISHED 상태만 (nickname 존재 시 조건 없음 → 전체 상태)
     */
    private static Specification<Post> onlyPublishedIfNicknameAbsent(String nickname) {
        return (root, query, cb) ->
                Objects.isNull(nickname)
                        ? cb.equal(root.get("status"), PostStatus.PUBLISHED)
                        : null;
    }

    /**
     * 항상 PUBLISHED 상태만
     */
    private static Specification<Post> onlyPublished() {
        return (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PUBLISHED);
    }

    /**
     * 닉네임 일치하는 사용자의 게시글만 (null이면 조건 없음 → 전체 사용자)
     */
    private static Specification<Post> nicknameEquals(String nickname) {
        return (root, query, cb) ->
                Objects.nonNull(nickname)
                        ? cb.equal(root.get("user").get("nickname"), nickname)
                        : null;
    }
}
