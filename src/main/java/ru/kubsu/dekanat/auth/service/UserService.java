package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.dto.CreateUserRequest;
import ru.kubsu.dekanat.auth.entity.Role;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Sort DEFAULT_SORT = Sort.by("lastName", "firstName", "id");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<User> getPage(int page, int perPage) {
        Pageable pageable = PageRequest.of(page - 1, perPage, DEFAULT_SORT);
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setActive(request.isActiveOrDefault());
        user.setRole(request.isAdminOrDefault() ? Role.ADMIN : Role.USER);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(email);
        }
    }

}
