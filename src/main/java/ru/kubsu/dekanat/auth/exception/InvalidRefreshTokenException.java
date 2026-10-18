package ru.kubsu.dekanat.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh-токен отсутствует, недействителен или истёк");
    }

}
