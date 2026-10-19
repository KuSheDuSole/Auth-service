package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.config.TotpProperties;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.entity.UserTotp;
import ru.kubsu.dekanat.auth.exception.InvalidConfirmationCodeException;
import ru.kubsu.dekanat.auth.exception.InvalidCurrentPasswordException;
import ru.kubsu.dekanat.auth.exception.TwoFactorStateException;
import ru.kubsu.dekanat.auth.exception.UserNotFoundException;
import ru.kubsu.dekanat.auth.repository.UserRepository;
import ru.kubsu.dekanat.auth.repository.UserTotpRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;
    private final UserTotpRepository userTotpRepository;
    private final TotpService totpService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final TotpProperties properties;

    @Transactional(readOnly = true)
    public boolean isEnabled(Long userId) {
        return userTotpRepository.findById(userId).map(UserTotp::isEnabled).orElse(false);
    }

    @Transactional
    public TotpSetup setup(Long userId, String currentPassword) {
        User user = getUserWithPassword(userId, currentPassword);

        UserTotp totp = userTotpRepository.findById(userId).orElseGet(UserTotp::new);
        if (totp.isEnabled()) throw new TwoFactorStateException("Двухфакторная аутентификация уже включена");

        String secret = totpService.generateSecret();
        totp.setUser(user);
        totp.setSecret(secret);
        totp.setLastUsedStep(null);
        userTotpRepository.save(totp);

        return new TotpSetup(secret, buildOtpauthUri(secret, user.getEmail()));
    }

    @Transactional
    public void enable(Long userId, String code) {
        UserTotp totp = userTotpRepository.findById(userId)
                .orElseThrow(() -> new TwoFactorStateException("Сначала начните настройку двухфакторной аутентификации"));
        if (totp.isEnabled()) throw new TwoFactorStateException("Двухфакторная аутентификация уже включена");

        verifyCode(totp, code);
        totp.setEnabled(true);

        User user = totp.getUser();
        mailService.send(user.getEmail(), "Двухфакторная аутентификация включена",
                "Здравствуйте, " + user.getFirstName() + "!\n\n"
                        + "Для вашей учётной записи в системе «Деканат» включена двухфакторная аутентификация.\n"
                        + "Если это сделали не вы, срочно обратитесь к администратору.");
    }

    @Transactional
    public void disable(Long userId, String currentPassword, String code) {
        User user = getUserWithPassword(userId, currentPassword);

        UserTotp totp = userTotpRepository.findById(userId)
                .filter(UserTotp::isEnabled)
                .orElseThrow(() -> new TwoFactorStateException("Двухфакторная аутентификация не включена"));

        verifyCode(totp, code);
        userTotpRepository.delete(totp);

        mailService.send(user.getEmail(), "Двухфакторная аутентификация отключена",
                "Здравствуйте, " + user.getFirstName() + "!\n\n"
                        + "Для вашей учётной записи в системе «Деканат» отключена двухфакторная аутентификация.\n"
                        + "Если это сделали не вы, срочно смените пароль и обратитесь к администратору.");
    }

    private User getUserWithPassword(Long userId, String currentPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        return user;
    }

    private void verifyCode(UserTotp totp, String code) {
        long step = totpService.verify(totp.getSecret(), code)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Неверный код"));
        if (totp.getLastUsedStep() != null && step <= totp.getLastUsedStep()) {
            throw new InvalidConfirmationCodeException("Этот код уже использован, дождитесь следующего");
        }
        totp.setLastUsedStep(step);
    }

    private String buildOtpauthUri(String secret, String email) {
        String issuer = encode(properties.issuer());
        return "otpauth://totp/" + issuer + ":" + encode(email) + "?secret=" + secret + "&issuer=" + issuer;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

}
