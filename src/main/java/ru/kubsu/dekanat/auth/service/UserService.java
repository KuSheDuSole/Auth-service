package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    // id в конце — чтобы однофамильцы не «прыгали» между страницами
    private static final Sort DEFAULT_SORT = Sort.by("lastName", "firstName", "id");

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }


    @Transactional(readOnly = true)
    public Page<User> getPage(int page, int perPage) {
        Pageable pageable = PageRequest.of(page - 1, perPage, DEFAULT_SORT);
        return userRepository.findAll(pageable);
    }

}
