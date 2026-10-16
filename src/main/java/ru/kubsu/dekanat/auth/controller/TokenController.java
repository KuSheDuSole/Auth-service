package ru.kubsu.dekanat.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.dekanat.auth.dto.LoginRequest;
import ru.kubsu.dekanat.auth.dto.TokenResponse;
import ru.kubsu.dekanat.auth.security.AppUserDetails;
import ru.kubsu.dekanat.auth.service.TokenService;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        var user = (AppUserDetails) authentication.getPrincipal();
        return tokenService.issueTokens(user, request.rememberMeOrDefault());
    }

}
