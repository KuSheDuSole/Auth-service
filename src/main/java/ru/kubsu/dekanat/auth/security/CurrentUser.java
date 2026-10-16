package ru.kubsu.dekanat.auth.security;

import ru.kubsu.dekanat.auth.entity.Role;


public record CurrentUser(Long id, Role role) {
}
