package ru.kubsu.dekanat.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

@ConfigurationProperties("app.jwt")
public record JwtProperties(
        RSAPrivateKey privateKey,
        RSAPublicKey publicKey,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        Duration rememberMeRefreshTokenTtl,
        Duration mfaTokenTtl
) {
}
