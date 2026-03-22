package com.sealog.backend.domain.feature.category.enums;

import com.sealog.backend.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryGroup {

    LANGUAGE("language", "언어"),
    FRAMEWORK("framework", "프레임워크"),
    LIBRARY("library","라이브러리"),
    DATABASE("database", "데이터베이스"),
    DEVOPS("devops", "데브옵스"),
    KNOWLEDGE("knowledge", "지식"),
    TOOL("tool", "툴"),
    ETC("etc", "기타");

    private final String key;
    private final String title;

    public static CategoryGroup fromKey(String key) {
        for (CategoryGroup group : values()) {
            if (group.key.equalsIgnoreCase(key)) {
                return group;
            }
        }
        throw CustomException.badRequest("유효하지 않은 카테고리 그룹입니다 : " + key);
    }
}
