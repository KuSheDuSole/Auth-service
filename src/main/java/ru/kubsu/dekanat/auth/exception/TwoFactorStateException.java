package ru.kubsu.dekanat.auth.exception;

public class TwoFactorStateException extends RuntimeException {

    public TwoFactorStateException(String message) {
        super(message);
    }

}
