package com.sealog.backend.global.constant;

import lombok.experimental.UtilityClass;

/**
 * 공용 Redis Key 상수 클래스
 */

@UtilityClass
public class CoreRedisKey {

    public static final String PREFIX_KEY = "KEY:";
    public static final String PREFIX_CACHE = "CACHE:";
    public static final String PREFIX_LOCK = "LOCK:";
}
