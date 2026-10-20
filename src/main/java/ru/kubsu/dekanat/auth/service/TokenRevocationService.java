package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.config.JwtProperties;
import ru.kubsu.dekanat.auth.repository.RefreshTokenRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    public static final String REVOKED_USER_KEY_PREFIX = "revoked:user:";

    private final StringRedisTemplate redis;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void revokeAllSessions(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
        revokeAccessTokens(userId);
    }

    public void revokeAccessTokens(Long userId) {
        String key = REVOKED_USER_KEY_PREFIX + userId;
        String revokedAt = String.valueOf(Instant.now().getEpochSecond());
        try {
            redis.opsForValue().set(key, revokedAt, jwtProperties.accessTokenTtl());
        } catch (DataAccessException e) {
            log.error("Не удалось записать {} в Redis — access-токены пользователя проживут до истечения срока", key, e);
        }
    }

}
