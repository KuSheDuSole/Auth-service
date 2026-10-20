package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import ru.kubsu.dekanat.auth.config.RateLimitProperties;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitService {

    private static final String KEY_PREFIX = "ratelimit:";

    private static final RedisScript<Long> INCREMENT_WITH_EXPIRE = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]) "
                    + "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end "
                    + "return count",
            Long.class);

    private final StringRedisTemplate redis;

    public RateLimitResult tryAcquire(String action, String subject, RateLimitProperties.Limit limit) {
        String cooldownKey = KEY_PREFIX + action + ":cooldown:" + subject;
        String windowKey = KEY_PREFIX + action + ":window:" + subject;
        try {
            Boolean cooldownStarted = redis.opsForValue().setIfAbsent(cooldownKey, "1", limit.cooldown());
            if (!Boolean.TRUE.equals(cooldownStarted)) {
                return RateLimitResult.deny(timeToLive(cooldownKey));
            }

            Long count = redis.execute(INCREMENT_WITH_EXPIRE, List.of(windowKey),
                    String.valueOf(limit.window().toSeconds()));
            if (count != null && count > limit.maxRequests()) {
                return RateLimitResult.deny(timeToLive(windowKey));
            }

            return RateLimitResult.allow();
        } catch (DataAccessException e) {
            log.error("Redis недоступен — лимит частоты для действия {} не проверен", action, e);
            return RateLimitResult.allow();
        }
    }

    private Duration timeToLive(String key) {
        Long millis = redis.getExpire(key, TimeUnit.MILLISECONDS);
        long seconds = millis == null || millis < 1 ? 1 : (millis + 999) / 1000;
        return Duration.ofSeconds(seconds);
    }

}
