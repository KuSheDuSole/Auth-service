package ru.kubsu.dekanat.auth.exception;

import java.time.Duration;

public class TwoFactorLockedException extends RuntimeException {

    public TwoFactorLockedException(Duration remaining) {
        super("Слишком много неверных кодов, попробуйте через " + (remaining.toMinutes() + 1) + " мин.");
    }

}
