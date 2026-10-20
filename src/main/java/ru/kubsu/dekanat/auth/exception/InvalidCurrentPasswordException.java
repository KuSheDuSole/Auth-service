package ru.kubsu.dekanat.auth.exception;

public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException() {
        super("Неверный текущий пароль");
    }

}
