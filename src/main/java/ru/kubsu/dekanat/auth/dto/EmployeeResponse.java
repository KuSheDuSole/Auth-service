package ru.kubsu.dekanat.auth.dto;

import ru.kubsu.dekanat.auth.entity.Role;
import ru.kubsu.dekanat.auth.entity.User;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean isActive,
        boolean isAdmin
) {

    public static EmployeeResponse from(User user) {
        return new EmployeeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isActive(),
                user.getRole() == Role.ADMIN);
    }

}
