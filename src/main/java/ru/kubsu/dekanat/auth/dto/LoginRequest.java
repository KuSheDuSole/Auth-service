package ru.kubsu.dekanat.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * Тело POST /token/. Стиль ключей с фронтом ещё не согласован,
 * поэтому remember_me принимаем в обоих вариантах.
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password,
        @JsonAlias("remember_me") Boolean rememberMe
) {

    public boolean rememberMeOrDefault() {
        return Boolean.TRUE.equals(rememberMe);
    }

}
