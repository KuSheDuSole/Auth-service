package ru.kubsu.dekanat.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorLoginRequest(
        @NotBlank String mfaToken,
        @NotBlank String code
) {
}
