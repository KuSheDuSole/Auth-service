package ru.kubsu.dekanat.auth.dto;

public record MfaRequiredResponse(boolean mfaRequired, String mfaToken) {
}
