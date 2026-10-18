package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.dto.CreateUserRequest;
import ru.kubsu.dekanat.auth.dto.UpdateProfileRequest;
import ru.kubsu.dekanat.auth.dto.UpdateUserRequest;
import ru.kubsu.dekanat.auth.entity.Role;
import ru.kubsu.dekanat.auth.exception.EmailAlreadyExistsException;
import ru.kubsu.dekanat.auth.exception.InvalidCurrentPasswordException;
import ru.kubsu.dekanat.auth.exception.UserNotFoundException;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.repository.UserRepository;
import ru.kubsu.dekanat.auth.security.CurrentUser;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Sort DEFAULT_SORT = Sort.by("lastName", "firstName", "id");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRevocationService tokenRevocationService;

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
    public User create(CreateUserRequest request, CurrentUser actor) {
        if (request.isAdminOrDefault() && actor.role() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Назначать администраторов может только супер-админ");
        }

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

    @Transactional
    public User update(Long id, UpdateUserRequest request, CurrentUser actor) {
        User user = getById(id);
        checkCanManage(actor, user);

        if (request.isActive() != null) {
            if (user.getRole() == Role.SUPER_ADMIN && !request.isActive()) {
                throw new AccessDeniedException("Супер-админа нельзя заблокировать");
            }
            if (user.isActive() && !request.isActive()) tokenRevocationService.revokeAllSessions(user.getId());
            user.setActive(request.isActive());
        }
        if (request.firstName() != null) user.setFirstName(request.firstName().trim());
        if (request.lastName() != null) user.setLastName(request.lastName().trim());

        if (request.email() != null) {
            String email = request.email().trim().toLowerCase();
            if (!email.equals(user.getEmail())) {
                if (userRepository.existsByEmail(email)) throw new EmailAlreadyExistsException(email);
                user.setEmail(email);
                try {
                    userRepository.flush();
                } catch (DataIntegrityViolationException e) {
                    throw new EmailAlreadyExistsException(email);
                }
            }
        }

        return user;
    }

    @Transactional
    public User updateProfile(Long id, UpdateProfileRequest request) {
        User user = getById(id);
        if (request.firstName() != null) user.setFirstName(request.firstName().trim());
        if (request.lastName() != null) user.setLastName(request.lastName().trim());
        return user;
    }

    @Transactional
    public void changeOwnPassword(Long id, String currentPassword, String newPassword) {
        User user = getById(id);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        tokenRevocationService.revokeAllSessions(user.getId());
    }

    @Transactional
    public User changeRole(Long id, boolean isAdmin) {
        User user = getById(id);
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Роль супер-админа изменить нельзя");
        }
        Role newRole = isAdmin ? Role.ADMIN : Role.USER;
        if (user.getRole() != newRole) {
            user.setRole(newRole);
            tokenRevocationService.revokeAccessTokens(user.getId());
        }
        return user;
    }

    @Transactional
    public void setPassword(Long id, String password, CurrentUser actor) {
        User user = getById(id);
        checkCanManage(actor, user);
        user.setPasswordHash(passwordEncoder.encode(password));
        tokenRevocationService.revokeAllSessions(user.getId());
    }

    @Transactional
    public void delete(Long id, CurrentUser actor) {
        User user = getById(id);
        if (user.getRole() == Role.SUPER_ADMIN) throw new AccessDeniedException("Супер-админа удалить нельзя");
        checkCanManage(actor, user);
        userRepository.delete(user);
        tokenRevocationService.revokeAccessTokens(user.getId());
    }

    private void checkCanManage(CurrentUser actor, User target) {
        boolean allowed = switch (target.getRole()) {
            case USER -> true;
            case ADMIN -> actor.role() == Role.SUPER_ADMIN;
            case SUPER_ADMIN -> target.getId().equals(actor.id());
        };
        if (!allowed) {
            throw new AccessDeniedException("Недостаточно прав для управления этим пользователем");
        }
    }

}
