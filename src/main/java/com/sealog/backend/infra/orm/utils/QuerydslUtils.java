package com.sealog.backend.infra.orm.utils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QuerydslUtils {

    /**
     * Hibernate 등록 함수를 BooleanExpression 으로 변환
     *
     * @param functionName Hibernate에 등록된 함수명
     * @param params       함수에 전달할 파라미터 (가변인자)
     * @return BooleanExpression
     */
    public static BooleanExpression createScoreBooleanExp(String functionName, Object... params) {

        // 1. template 생성
        StringBuilder template = buildFunctionTemplate(functionName, params);
        template.append(")");

        // MATCH AGAINST는 relevance score(float)를 반환하므로 > 0 비교
        // = true 는 SQL에서 = 1 로 변환되어 정확히 1.0인 경우만 매칭됨 → 사실상 0건
        NumberTemplate<Double> scoreTemplate = Expressions.numberTemplate(Double.class, template.toString(), params);
        return scoreTemplate.gt(0);
    }


    /**
     * Hibernate 등록 함수를 BooleanExpression 으로 변환
     *
     * @param functionName Hibernate에 등록된 함수명
     * @param params       함수에 전달할 파라미터 (가변인자)
     * @return BooleanExpression
     */
    public static BooleanExpression booleanFunction(String functionName, Object... params) {

        // 1. template 생성
        StringBuilder template = buildFunctionTemplate(functionName, params);

        // Hibernate JPQL 파서가 predicate context에서 Boolean 함수로 인식하려면 = true 명시 필요
        template.append(") = true");

        // 2. template + args 조합으로 BooleanExpression 생성
        return Expressions.booleanTemplate(template.toString(), params);
    }



    private static StringBuilder buildFunctionTemplate(String functionName, Object[] params) {

        // {0}, {1}, {2}... 인덱스 문자열 동적 생성
        StringBuilder template = new StringBuilder("function('")
                .append(functionName)
                .append("'");

        for (int i = 0; i < params.length; i++) {
            template.append(", {").append(i).append("}");
        }
        return template;
    }
}
