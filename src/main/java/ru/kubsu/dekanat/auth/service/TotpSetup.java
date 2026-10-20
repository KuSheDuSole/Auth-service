package ru.kubsu.dekanat.auth.service;

public record TotpSetup(String secret, String otpauthUri) {
}
