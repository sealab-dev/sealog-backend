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
 * - requesterId = null → Guest (PUBLISHED만, 전체 사용자)
 * - requesterId != null → User (전체 상태, 본인 게시글만)
 */
@UtilityClass
public class PostCondition {

    /**
     * 게시글 검색 조건 조합
     * - 삭제되지 않은 게시글 + 키워드 매칭 + 역할별 조건
     *
     * @param requesterId 요청자 ID (null = Guest, non-null = User)
     * @param keyword     검색 키워드
     * @return 조합된 Specification
     */
    public static Specification<Post> search(Long requesterId, String keyword) {
        return Specification.allOf(
                notDeleted(),
                keywordContains(keyword),
                onlyPublishedIfGuest(requesterId),
                ownedByUser(requesterId)
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
     * Guest인 경우 PUBLISHED 상태만 (User는 조건 없음 → 전체 상태)
     */
    private static Specification<Post> onlyPublishedIfGuest(Long requesterId) {
        return (root, query, cb) ->
                Objects.isNull(requesterId)
                        ? cb.equal(root.get("status"), PostStatus.PUBLISHED)
                        : null;
    }

    /**
     * User인 경우 본인 게시글만 (Guest는 조건 없음 → 전체 사용자)
     */
    private static Specification<Post> ownedByUser(Long requesterId) {
        return (root, query, cb) ->
                Objects.nonNull(requesterId)
                        ? cb.equal(root.get("user").get("id"), requesterId)
                        : null;
    }
}
