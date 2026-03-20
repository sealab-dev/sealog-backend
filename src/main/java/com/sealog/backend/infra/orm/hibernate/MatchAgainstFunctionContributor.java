package com.sealog.backend.infra.orm.hibernate;

import com.sealog.backend.infra.orm.constant.HibernateFunction;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

/**
 * MariaDB MATCH...AGAINST 함수를 Hibernate Criteria API에 등록
 *
 * 사용 목적:
 * - FULLTEXT 인덱스 기반 검색 (LIKE 풀스캔 대체)
 * - PostCondition.keywordContainsFT()에서 cb.function(MATCH_AGAINST, ...) 형태로 사용
 */
public class MatchAgainstFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry().registerPattern(
                HibernateFunction.MATCH_AGAINST,
                "MATCH(?1, ?2) AGAINST (CONCAT(?3, '*') IN BOOLEAN MODE)",
                functionContributions.getTypeConfiguration()
                        .getBasicTypeRegistry()
                        .resolve(StandardBasicTypes.BOOLEAN)
        );
    }
}
