package com.sealog.backend.domain.feature.post.repository.condition;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Post 검색 조건 Specification 모음
 */
@UtilityClass
public class PostCondition {

    /**
     * 전체 공개 게시글 검색
     */
    public static Specification<Post> searchPosts(String keyword) {
        return Specification.allOf(
                notDeleted(),
                onlyPublished(),
                keywordContains(keyword)
        );
    }

    /**
     * 특정 사용자의 공개 게시글 검색
     */
    public static Specification<Post> searchByNickname(String nickname, String keyword) {
        return Specification.allOf(
                notDeleted(),
                onlyPublished(),
                nicknameEquals(nickname),
                keywordContains(keyword)
        );
    }

    /**
     * 내 게시글 검색 (전체 상태 포함)
     */
    public static Specification<Post> searchMe(String nickname, String keyword) {
        return Specification.allOf(
                notDeleted(),
                nicknameEquals(nickname),
                keywordContains(keyword)
        );
    }

    // ========== private conditions ========== //

    private static Specification<Post> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private static Specification<Post> keywordContains(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            return cb.or(
                    cb.like(root.get("title"), "%" + keyword + "%"),
                    cb.like(root.get("excerpt"), "%" + keyword + "%")
            );
        };
    }

    private static Specification<Post> onlyPublished() {
        return (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PUBLISHED);
    }

    private static Specification<Post> nicknameEquals(String nickname) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(nickname)) return null;
            return cb.equal(root.get("user").get("nickname"), nickname);
        };
    }
}
