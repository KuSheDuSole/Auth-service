package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.repository.EmailChangeCodeRepository;
import ru.kubsu.dekanat.auth.repository.PasswordResetTokenRepository;
import ru.kubsu.dekanat.auth.repository.RefreshTokenRepository;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredTokensCleanup {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailChangeCodeRepository emailChangeCodeRepository;

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.zone}")
    @Transactional
    public void cleanup() {
        Instant now = Instant.now();
        int refreshTokens = refreshTokenRepository.deleteAllExpired(now);
        int resetTokens = passwordResetTokenRepository.deleteAllExpired(now);
        int emailCodes = emailChangeCodeRepository.deleteAllExpired(now);
        log.info("Очистка протухших записей: refresh_tokens={}, password_reset_tokens={}, email_change_codes={}",
                refreshTokens, resetTokens, emailCodes);
    }

}
