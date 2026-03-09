package com.sealog.backend.support.constant;

import lombok.experimental.UtilityClass;

/**
 * 테스트 컨테이너 관련 상수를 관리하는 클래스
 */
@UtilityClass
public class TestContainer {

    public static final String DEFAULT_IMAGE_DATABASE = System.getenv().getOrDefault("IMAGE_DATABASE", "mariadb:11.4.8");
    public static final String DEFAULT_IMAGE_REDIS = System.getenv().getOrDefault("IMAGE_REDIS", "redis:7.2");

    public static final String DEFAULT_DATABASE_NAME = "sealog_test";
    public static final String DEFAULT_DATABASE_USERNAME = "test";
    public static final String DEFAULT_DATABASE_PASSWORD = "test";
    public static final int DEFAULT_REDIS_PORT = 6379;
}
