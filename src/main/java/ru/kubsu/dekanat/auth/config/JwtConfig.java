package ru.kubsu.dekanat.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import ru.kubsu.dekanat.auth.service.TokenService;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        return NimbusJwtEncoder.withKeyPair(jwtProperties.publicKey(), jwtProperties.privateKey()).build();
    }

    @Bean
    JwtDecoder refreshTokenDecoder(JwtProperties jwtProperties) {
        return decoderForTokenType(jwtProperties, TokenService.REFRESH);
    }

    @Bean
    JwtDecoder mfaTokenDecoder(JwtProperties jwtProperties) {
        return decoderForTokenType(jwtProperties, TokenService.MFA);
    }

    private static JwtDecoder decoderForTokenType(JwtProperties jwtProperties, String tokenType) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(jwtProperties.publicKey()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()),
                new JwtClaimValidator<String>(TokenService.TOKEN_TYPE_CLAIM, tokenType::equals)));
        return decoder;
    }

}
