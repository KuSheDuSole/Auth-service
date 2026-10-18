package ru.kubsu.dekanat.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.dekanat.auth.config.JwtProperties;
import ru.kubsu.dekanat.auth.entity.RefreshToken;
import ru.kubsu.dekanat.auth.entity.User;
import ru.kubsu.dekanat.auth.exception.InvalidRefreshTokenException;
import ru.kubsu.dekanat.auth.repository.RefreshTokenRepository;
import ru.kubsu.dekanat.auth.repository.UserRepository;
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
    private final JwtDecoder refreshTokenDecoder;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenPair issueTokens(AppUserDetails user, boolean rememberMe) {
        Duration refreshTtl = rememberMe ? jwtProperties.rememberMeRefreshTokenTtl() : jwtProperties.refreshTokenTtl();
        String refreshJti = UUID.randomUUID().toString();
        Instant now = Instant.now();

        RefreshToken stored = new RefreshToken();
        stored.setJti(refreshJti);
        stored.setUser(userRepository.getReferenceById(user.id()));
        stored.setExpiresAt(now.plus(refreshTtl));
        refreshTokenRepository.save(stored);

        return new TokenPair(
                encode(user, ACCESS, UUID.randomUUID().toString(), now, jwtProperties.accessTokenTtl()),
                encode(user, REFRESH, refreshJti, now, refreshTtl),
                refreshTtl);
    }

    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) throw new InvalidRefreshTokenException();

        Jwt jwt;
        try {
            jwt = refreshTokenDecoder.decode(refreshToken);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }

        if (!refreshTokenRepository.existsById(jwt.getId())) throw new InvalidRefreshTokenException();

        AppUserDetails user = userRepository.findById(Long.valueOf(jwt.getSubject()))
                .filter(User::isActive)
                .map(AppUserDetails::from)
                .orElseThrow(InvalidRefreshTokenException::new);

        return encode(user, ACCESS, UUID.randomUUID().toString(), Instant.now(), jwtProperties.accessTokenTtl());
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        try {
            Jwt jwt = refreshTokenDecoder.decode(refreshToken);
            refreshTokenRepository.deleteById(jwt.getId());
        } catch (JwtException e) {
            // протухший или поддельный токен — отзывать нечего
        }
    }

    private String encode(AppUserDetails user, String tokenType, String jti, Instant now, Duration ttl) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.id().toString())
                .id(jti)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim(ROLE_CLAIM, user.role().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
