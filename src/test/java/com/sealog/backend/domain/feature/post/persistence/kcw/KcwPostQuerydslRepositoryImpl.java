package com.sealog.backend.domain.feature.post.persistence.kcw;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sealog.backend.domain.feature.post.entity.QPost;
import com.sealog.backend.domain.feature.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Post QueryDSL 기반 조회 Repository (kcw 실험용)
 *
 * 사용 목적:
 * - DTO 직접 반환으로 content(@Lob) 컬럼 제외
 * - 엔티티 반환(Specification) 대비 성능 차이 측정
 */
@RequiredArgsConstructor
public class KcwPostQuerydslRepositoryImpl implements KcwPostQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    private static final QPost post = QPost.post;
    private static final QUser user = QUser.user;

    @Override
    public Page<PostSummary> searchLike(String nickname, String keyword, Pageable pageable) {
        return processSearch(pageable, KcwPostQueryCondition.search(nickname, keyword));
    }

    @Override
    public Page<PostSummary> searchFt(String nickname, String keyword, Pageable pageable) {
        return processSearch(pageable, KcwPostQueryCondition.searchFt(nickname, keyword));
    }

    private Page<PostSummary> processSearch(
            Pageable pageable,
            BooleanExpression condition
    ) {

        // 메인 쿼리
        List<PostSummary> content = queryFactory
                .select(Projections.constructor(PostSummary.class,
                        post.id,
                        post.title,
                        post.excerpt,
                        post.slug,
                        post.status,
                        post.thumbnailPath
                ))
                .from(post)
                .join(post.user, user)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .join(post.user, user)
                .where(condition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchFirst);
    }
}
