package ru.kubsu.dekanat.auth.controller;

import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@NoArgsConstructor
final class RefreshTokenCookie {

    static final String NAME = "refresh_token";
    private static final String PATH = "/api/v1/token";

    static ResponseCookie create(String refreshToken, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = baseBuilder(refreshToken);
        if (maxAge != null) builder.maxAge(maxAge);
        return builder.build();
    }

    static ResponseCookie clear() {
        return baseBuilder("").maxAge(Duration.ZERO).build();
    }

    private static ResponseCookie.ResponseCookieBuilder baseBuilder(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(PATH);
    }

}
