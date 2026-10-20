package ru.kubsu.dekanat.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.dekanat.auth.dto.TwoFactorDisableRequest;
import ru.kubsu.dekanat.auth.dto.TwoFactorEnableRequest;
import ru.kubsu.dekanat.auth.dto.TwoFactorSetupRequest;
import ru.kubsu.dekanat.auth.dto.TwoFactorSetupResponse;
import ru.kubsu.dekanat.auth.dto.TwoFactorStatusResponse;
import ru.kubsu.dekanat.auth.security.CurrentUser;
import ru.kubsu.dekanat.auth.service.TotpSetup;
import ru.kubsu.dekanat.auth.service.TwoFactorService;

@RestController
@RequestMapping("/api/v1/users/me/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @GetMapping
    public TwoFactorStatusResponse status(@AuthenticationPrincipal CurrentUser currentUser) {
        return new TwoFactorStatusResponse(twoFactorService.isEnabled(currentUser.id()));
    }

    @PostMapping("/setup")
    public TwoFactorSetupResponse setup(@Valid @RequestBody TwoFactorSetupRequest request,
                                        @AuthenticationPrincipal CurrentUser currentUser) {
        TotpSetup setup = twoFactorService.setup(currentUser.id(), request.currentPassword());
        return new TwoFactorSetupResponse(setup.secret(), setup.otpauthUri());
    }

    @PostMapping("/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@Valid @RequestBody TwoFactorEnableRequest request,
                       @AuthenticationPrincipal CurrentUser currentUser) {
        twoFactorService.enable(currentUser.id(), request.code());
    }

    @PostMapping("/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@Valid @RequestBody TwoFactorDisableRequest request,
                        @AuthenticationPrincipal CurrentUser currentUser) {
        twoFactorService.disable(currentUser.id(), request.currentPassword(), request.code());
    }

}
