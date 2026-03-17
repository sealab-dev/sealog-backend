package com.sealog.backend.infra.redis.repository;

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

    public void save(Long userId, String token, long ttlMillis) {
        stringRedisTemplate.opsForValue()
                .set(CoreRedisKey.PREFIX_REFRESH_TOKEN + userId, token, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public Optional<String> find(Long userId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(CoreRedisKey.PREFIX_REFRESH_TOKEN + userId));
    }

    public void delete(Long userId) {
        stringRedisTemplate.delete(CoreRedisKey.PREFIX_REFRESH_TOKEN + userId);
    }
}
