package ru.kubsu.dekanat.auth.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull Boolean isAdmin
) {
}
