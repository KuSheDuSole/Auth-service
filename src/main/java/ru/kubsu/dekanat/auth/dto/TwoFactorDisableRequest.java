package ru.kubsu.dekanat.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorDisableRequest(
        @NotBlank String currentPassword,
        @NotBlank String code
) {
}
