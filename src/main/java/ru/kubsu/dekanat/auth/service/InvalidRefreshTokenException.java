package ru.kubsu.dekanat.auth.service;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh-токен отсутствует, недействителен или истёк");
    }

}
