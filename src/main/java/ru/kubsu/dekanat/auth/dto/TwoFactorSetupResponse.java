package ru.kubsu.dekanat.auth.dto;

public record TwoFactorSetupResponse(String secret, String otpauthUri) {
}
