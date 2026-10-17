package ru.kubsu.dekanat.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.dekanat.auth.dto.AccessTokenResponse;
import ru.kubsu.dekanat.auth.dto.LoginRequest;
import ru.kubsu.dekanat.auth.security.AppUserDetails;
import ru.kubsu.dekanat.auth.service.TokenPair;
import ru.kubsu.dekanat.auth.service.TokenService;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        boolean rememberMe = request.rememberMeOrDefault();
        TokenPair tokens = tokenService.issueTokens(user, rememberMe);

        ResponseCookie cookie = RefreshTokenCookie.create(tokens.refreshToken(), rememberMe ? tokens.refreshTokenTtl() : null);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public AccessTokenResponse refresh(
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
        return new AccessTokenResponse(tokenService.refreshAccessToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.clear().toString())
                .build();
    }

}
