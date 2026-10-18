package ru.kubsu.dekanat.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.dekanat.auth.dto.ChangePasswordRequest;
import ru.kubsu.dekanat.auth.dto.ChangeRoleRequest;
import ru.kubsu.dekanat.auth.dto.CreateUserRequest;
import ru.kubsu.dekanat.auth.dto.EmployeeResponse;
import ru.kubsu.dekanat.auth.dto.PageResponse;
import ru.kubsu.dekanat.auth.dto.SetPasswordRequest;
import ru.kubsu.dekanat.auth.dto.UpdateProfileRequest;
import ru.kubsu.dekanat.auth.dto.UpdateUserRequest;
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

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request,
                                 @AuthenticationPrincipal CurrentUser currentUser) {
        User user = userService.updateProfile(currentUser.id(), request);
        return new UserResponse(EmployeeResponse.from(user));
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyPassword(@Valid @RequestBody ChangePasswordRequest request,
                                 @AuthenticationPrincipal CurrentUser currentUser) {
        userService.changeOwnPassword(currentUser.id(), request.currentPassword(), request.newPassword());
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request,
                               @AuthenticationPrincipal CurrentUser currentUser) {
        User user = userService.create(request, currentUser);
        return new UserResponse(EmployeeResponse.from(user));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request,
                               @AuthenticationPrincipal CurrentUser currentUser) {
        User user = userService.update(id, request, currentUser);
        return new UserResponse(EmployeeResponse.from(user));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        userService.delete(id, currentUser);
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void setPassword(@PathVariable Long id, @Valid @RequestBody SetPasswordRequest request,
                            @AuthenticationPrincipal CurrentUser currentUser) {
        userService.setPassword(id, request.password(), currentUser);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
        User user = userService.changeRole(id, request.isAdmin());
        return new UserResponse(EmployeeResponse.from(user));
    }

}
