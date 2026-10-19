package ru.kubsu.dekanat.auth.exception;

public class InvalidMfaTokenException extends RuntimeException {

    public InvalidMfaTokenException() {
        super("Время на ввод кода истекло, войдите заново");
    }

}
