package ru.kubsu.dekanat.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorSetupRequest(
        @NotBlank String currentPassword
) {
}
