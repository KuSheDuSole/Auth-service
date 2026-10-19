package ru.kubsu.dekanat.auth.service;

import ru.kubsu.dekanat.auth.security.AppUserDetails;

public record MfaChallenge(AppUserDetails user, boolean rememberMe) {
}
