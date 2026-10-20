package ru.kubsu.dekanat.auth.exception;

import lombok.Getter;

import java.time.Duration;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final Duration retryAfter;

    public RateLimitExceededException(Duration retryAfter) {
        super("Слишком много запросов, повторите через " + format(retryAfter));
        this.retryAfter = retryAfter;
    }

    private static String format(Duration duration) {
        long seconds = duration.toSeconds();
        if (seconds < 60) return seconds + " сек.";
        return (seconds + 59) / 60 + " мин.";
    }

}
