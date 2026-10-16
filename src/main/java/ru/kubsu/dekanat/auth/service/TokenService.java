package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.kubsu.dekanat.auth.config.JwtProperties;
import ru.kubsu.dekanat.auth.dto.TokenResponse;
import ru.kubsu.dekanat.auth.security.AppUserDetails;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String ROLE_CLAIM = "role";
    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenResponse issueTokens(AppUserDetails user, boolean rememberMe) {
        Duration refreshTtl = rememberMe ? jwtProperties.rememberMeRefreshTokenTtl() : jwtProperties.refreshTokenTtl();
        return new TokenResponse(
                encode(user, ACCESS, jwtProperties.accessTokenTtl()),
                encode(user, REFRESH, refreshTtl));
    }

    private String encode(AppUserDetails user, String tokenType, Duration ttl) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.id().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim(ROLE_CLAIM, user.role().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
