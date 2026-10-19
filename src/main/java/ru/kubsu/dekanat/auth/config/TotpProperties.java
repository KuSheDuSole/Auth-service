package ru.kubsu.dekanat.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.totp")
public record TotpProperties(
        String encryptionKey,
        String issuer,
        int maxAttempts,
        Duration lockout
) {
}
