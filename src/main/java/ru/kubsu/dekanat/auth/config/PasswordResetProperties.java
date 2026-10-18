package ru.kubsu.dekanat.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.password-reset")
public record PasswordResetProperties(
        String url,
        Duration tokenTtl
) {
}
