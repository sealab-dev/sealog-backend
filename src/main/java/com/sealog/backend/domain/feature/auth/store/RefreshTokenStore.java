package com.sealog.backend.domain.feature.auth.store;

import com.sealog.backend.global.constant.CoreRedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate stringRedisTemplate;
    private final String PREFIX_REFRESH_TOKEN = CoreRedisKey.PREFIX_KEY + "RT:";

    public void save(Long userId, String token, long ttlMillis) {
        stringRedisTemplate.opsForValue()
                .set(PREFIX_REFRESH_TOKEN + userId, token, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public Optional<String> find(Long userId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(PREFIX_REFRESH_TOKEN + userId));
    }

    public void delete(Long userId) {
        stringRedisTemplate.delete(PREFIX_REFRESH_TOKEN + userId);
    }
}
