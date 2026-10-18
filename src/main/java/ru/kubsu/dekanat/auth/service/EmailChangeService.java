package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.config.EmailChangeProperties;
import ru.kubsu.dekanat.auth.entity.EmailChangeCode;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.exception.EmailAlreadyExistsException;
import ru.kubsu.dekanat.auth.exception.InvalidConfirmationCodeException;
import ru.kubsu.dekanat.auth.exception.InvalidCurrentPasswordException;
import ru.kubsu.dekanat.auth.exception.UserNotFoundException;
import ru.kubsu.dekanat.auth.repository.EmailChangeCodeRepository;
import ru.kubsu.dekanat.auth.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(EmailChangeProperties.class)
public class EmailChangeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailChangeCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final EmailChangeProperties properties;

    @Transactional
    public void requestChange(Long userId, String newEmail, String currentPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }

        String email = newEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) throw new EmailAlreadyExistsException(email);

        codeRepository.deleteAllByUser(user);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        EmailChangeCode changeCode = new EmailChangeCode();
        changeCode.setUser(user);
        changeCode.setNewEmail(email);
        changeCode.setCodeHash(Hashing.sha256(code));
        changeCode.setExpiresAt(Instant.now().plus(properties.codeTtl()));
        codeRepository.save(changeCode);

        mailService.send(email, "Подтверждение смены email",
                "Здравствуйте, " + user.getFirstName() + "!\n\n"
                        + "Код для подтверждения нового email в системе «Деканат»: " + code + "\n\n"
                        + "Код действует " + properties.codeTtl().toMinutes() + " минут.\n"
                        + "Если вы не меняли email, просто проигнорируйте это письмо.");
    }

    @Transactional(noRollbackFor = InvalidConfirmationCodeException.class)
    public User confirmChange(Long userId, String code) {
        EmailChangeCode changeCode = codeRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Смена email не запрашивалась"));
        User user = changeCode.getUser();

        if (changeCode.getExpiresAt().isBefore(Instant.now())) {
            codeRepository.delete(changeCode);
            throw new InvalidConfirmationCodeException("Код устарел, запросите новый");
        }

        if (!Hashing.sha256(code.trim()).equals(changeCode.getCodeHash())) {
            changeCode.setAttempts(changeCode.getAttempts() + 1);
            if (changeCode.getAttempts() >= properties.maxAttempts()) {
                codeRepository.delete(changeCode);
                throw new InvalidConfirmationCodeException("Слишком много неверных попыток, запросите новый код");
            }
            throw new InvalidConfirmationCodeException("Неверный код");
        }

        String oldEmail = user.getEmail();
        String newEmail = changeCode.getNewEmail();
        codeRepository.delete(changeCode);
        user.setEmail(newEmail);
        try {
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(newEmail);
        }

        mailService.send(oldEmail, "Email изменён",
                "Здравствуйте, " + user.getFirstName() + "!\n\n"
                        + "Email вашей учётной записи в системе «Деканат» изменён на " + newEmail + ".\n"
                        + "Если это сделали не вы, срочно обратитесь к администратору.");
        return user;
    }

}
