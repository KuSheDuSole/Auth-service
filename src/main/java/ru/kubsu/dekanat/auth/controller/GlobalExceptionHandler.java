package ru.kubsu.dekanat.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.kubsu.dekanat.auth.exception.EmailAlreadyExistsException;
import ru.kubsu.dekanat.auth.exception.InvalidConfirmationCodeException;
import ru.kubsu.dekanat.auth.exception.InvalidCurrentPasswordException;
import ru.kubsu.dekanat.auth.exception.InvalidMfaTokenException;
import ru.kubsu.dekanat.auth.exception.InvalidRefreshTokenException;
import ru.kubsu.dekanat.auth.exception.InvalidResetTokenException;
import ru.kubsu.dekanat.auth.exception.TwoFactorLockedException;
import ru.kubsu.dekanat.auth.exception.TwoFactorStateException;
import ru.kubsu.dekanat.auth.exception.UserNotFoundException;


@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException e) {

        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(InvalidMfaTokenException.class)
    ProblemDetail handleInvalidMfaToken(InvalidMfaTokenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(TwoFactorLockedException.class)
    ProblemDetail handleTwoFactorLocked(TwoFactorLockedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    // Бросается @PreAuthorize, когда у пользователя нет нужной роли
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Недостаточно прав");
    }

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    ProblemDetail handleInvalidCurrentPassword(InvalidCurrentPasswordException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    ProblemDetail handleInvalidResetToken(InvalidResetTokenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidConfirmationCodeException.class)
    ProblemDetail handleInvalidConfirmationCode(InvalidConfirmationCodeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(TwoFactorStateException.class)
    ProblemDetail handleTwoFactorState(TwoFactorStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

}
