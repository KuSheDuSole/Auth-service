package ru.kubsu.dekanat.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.email-change")
public record EmailChangeProperties(
        Duration codeTtl,
        int maxAttempts
) {
}
