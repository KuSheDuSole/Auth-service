package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.config.PasswordResetProperties;
import ru.kubsu.dekanat.auth.entity.PasswordResetToken;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.exception.InvalidResetTokenException;
import ru.kubsu.dekanat.auth.repository.PasswordResetTokenRepository;
import ru.kubsu.dekanat.auth.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(PasswordResetProperties.class)
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final TokenRevocationService tokenRevocationService;
    private final PasswordResetProperties properties;

    @Transactional
    public void requestReset(String email) {
        Optional<User> found = userRepository.findByEmail(email.trim().toLowerCase()).filter(User::isActive);
        if (found.isEmpty()) return;
        User user = found.get();

        tokenRepository.deleteAllByUser(user);

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(Hashing.sha256(token));
        resetToken.setExpiresAt(Instant.now().plus(properties.tokenTtl()));
        tokenRepository.save(resetToken);

        String link = properties.url() + "?token=" + token;
        mailService.send(user.getEmail(), "Восстановление пароля",
                "Здравствуйте, " + user.getFirstName() + "!\n\n"
                        + "Чтобы задать новый пароль в системе «Деканат», перейдите по ссылке:\n"
                        + link + "\n\n"
                        + "Ссылка действует " + properties.tokenTtl().toMinutes() + " минут и сработает один раз.\n"
                        + "Если вы не запрашивали восстановление, просто проигнорируйте это письмо.");
    }

    @Transactional
    public void confirmReset(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(Hashing.sha256(token))
                .orElseThrow(InvalidResetTokenException::new);
        User user = resetToken.getUser();

        if (resetToken.getExpiresAt().isBefore(Instant.now()) || !user.isActive()) {
            throw new InvalidResetTokenException();
        }

        tokenRepository.deleteAllByUser(user);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        tokenRevocationService.revokeAllSessions(user.getId());
    }

}
