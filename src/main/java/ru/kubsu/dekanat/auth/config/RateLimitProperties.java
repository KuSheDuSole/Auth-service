package ru.kubsu.dekanat.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
        Limit passwordReset,
        Limit emailChange
) {

    public record Limit(Duration cooldown, int maxRequests, Duration window) {
    }

}
