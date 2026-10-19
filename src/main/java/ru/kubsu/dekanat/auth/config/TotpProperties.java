package ru.kubsu.dekanat.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.totp")
public record TotpProperties(
        String encryptionKey
) {
}
