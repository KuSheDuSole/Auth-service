package ru.kubsu.dekanat.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.entity.Role;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminProperties.class)
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) return;

        if (adminProperties.password() == null || adminProperties.password().isBlank()) {
            log.warn("В БД нет супер-админа, но ADMIN_PASSWORD не задан — супер-админ не создан");
            return;
        }

        String email = adminProperties.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            log.warn("Пользователь {} уже существует, но не является супер-админом — супер-админ не создан", email);
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminProperties.password()));
        admin.setFirstName(adminProperties.firstName());
        admin.setLastName(adminProperties.lastName());
        admin.setRole(Role.SUPER_ADMIN);
        userRepository.save(admin);

        log.info("Создан супер-админ {}", email);
    }

}
