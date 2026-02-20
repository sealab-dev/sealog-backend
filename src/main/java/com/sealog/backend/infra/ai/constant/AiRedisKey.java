package com.sealog.backend.infra.ai.constant;

import com.sealog.backend.global.core.constant.CoreRedisKey;
import lombok.experimental.UtilityClass;

/**
 * AI와 관련한 Redis Key 상수 클래스
 */

@UtilityClass
public class AiRedisKey {

    public static final String CACHE_EMBEDDING = CoreRedisKey.PREFIX_CACHE + "EMBEDDING";
}
