package com.sealog.backend.domain.feature.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialType {

    GITHUB("github", "깃허브"),
    LINKEDIN("linkedin", "링크드인"),
    YOUTUBE("youtube", "유튜브"),
    INSTAGRAM("instagram", "인스타그램"),
    PORTFOLIO("portfolio", "포트폴리오");

    private final String key;
    private final String title;
}
