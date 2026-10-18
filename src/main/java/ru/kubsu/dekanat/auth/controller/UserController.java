package ru.kubsu.dekanat.auth.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.dekanat.auth.dto.EmployeeResponse;
import ru.kubsu.dekanat.auth.dto.PageResponse;
import ru.kubsu.dekanat.auth.dto.UserResponse;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.security.CurrentUser;
import ru.kubsu.dekanat.auth.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        User user = userService.getById(currentUser.id());
        return new UserResponse(EmployeeResponse.from(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<EmployeeResponse> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int perPage) {
        Page<EmployeeResponse> users = userService.getPage(page, perPage).map(EmployeeResponse::from);
        return PageResponse.from(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse get(@PathVariable Long id) {
        User user = userService.getById(id);
        return new UserResponse(EmployeeResponse.from(user));
    }

}
