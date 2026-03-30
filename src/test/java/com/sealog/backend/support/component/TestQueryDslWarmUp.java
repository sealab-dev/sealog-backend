package com.sealog.backend.support.component;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sealog.backend.domain.feature.user.entity.QUser;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestQueryDslWarmUp {

    private final JPAQueryFactory jpaQueryFactory;

    private static final QUser user = QUser.user;

    /**
     * 최초로 Querydsl 실행 시 아래의 클래스 정보 로딩
     * JPAQueryFactory → AbstractJPAQuery → HibernateQuery → Q Type Class
     */
    public void warmUp() {
        jpaQueryFactory
                .selectOne()    // SELECT 1
                .from(user)     // FROM user
                .limit(1)       // LIMIT 1
                .fetchFirst();  // 가장 처음 결과만 반환 (최대 1건, 없으면 null)
    }
}
