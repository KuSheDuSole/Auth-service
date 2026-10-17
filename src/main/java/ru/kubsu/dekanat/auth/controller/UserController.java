package ru.kubsu.dekanat.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.dekanat.auth.dto.CurrentUserResponse;
import ru.kubsu.dekanat.auth.dto.EmployeeResponse;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.security.CurrentUser;
import ru.kubsu.dekanat.auth.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        User user = userService.getById(currentUser.id());
        return new CurrentUserResponse(EmployeeResponse.from(user));
    }

}
