package ru.kubsu.dekanat.auth.exception;

public class InvalidConfirmationCodeException extends RuntimeException {

    public InvalidConfirmationCodeException(String message) {
        super(message);
    }

}
