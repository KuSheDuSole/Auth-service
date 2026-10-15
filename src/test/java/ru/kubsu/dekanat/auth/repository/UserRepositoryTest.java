package ru.kubsu.dekanat.auth.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import ru.kubsu.dekanat.auth.TestcontainersConfiguration;
import ru.kubsu.dekanat.auth.entity.Role;
import ru.kubsu.dekanat.auth.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesUserAndFindsByEmail() {
        userRepository.saveAndFlush(newUser("ivanov@kubsu.ru"));

        var found = userRepository.findByEmail("ivanov@kubsu.ru");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getRole()).isEqualTo(Role.USER);
        assertThat(found.get().isActive()).isTrue();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void findByEmailReturnsEmptyForUnknownEmail() {
        assertThat(userRepository.findByEmail("nobody@kubsu.ru")).isEmpty();
    }

    @Test
    void existsByEmail() {
        userRepository.saveAndFlush(newUser("petrov@kubsu.ru"));

        assertThat(userRepository.existsByEmail("petrov@kubsu.ru")).isTrue();
        assertThat(userRepository.existsByEmail("sidorov@kubsu.ru")).isFalse();
    }

    @Test
    void emailMustBeUnique() {
        userRepository.saveAndFlush(newUser("same@kubsu.ru"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(newUser("same@kubsu.ru")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static User newUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        return user;
    }

}
