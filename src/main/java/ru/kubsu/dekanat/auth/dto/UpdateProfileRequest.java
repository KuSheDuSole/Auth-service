package ru.kubsu.dekanat.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Pattern(regexp = ".*\\S.*", message = "не должно быть пустым") @Size(max = 255) String firstName,
        @Pattern(regexp = ".*\\S.*", message = "не должно быть пустым") @Size(max = 255) String lastName
) {
}
