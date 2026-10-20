package ru.kubsu.dekanat.auth.service;

import java.time.Duration;

public record TokenPair(String accessToken, String refreshToken, Duration refreshTokenTtl) {
}
