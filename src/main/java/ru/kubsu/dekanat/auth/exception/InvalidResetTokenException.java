package ru.kubsu.dekanat.auth.exception;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Ссылка для восстановления пароля недействительна или устарела");
    }

}
