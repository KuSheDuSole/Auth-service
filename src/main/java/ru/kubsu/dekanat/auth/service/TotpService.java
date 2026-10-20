package ru.kubsu.dekanat.auth.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.OptionalLong;

@Service
public class TotpService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int SECRET_BYTES = 20;
    private static final int PERIOD_SECONDS = 30;
    private static final int ALLOWED_DRIFT_STEPS = 1;

    private final Base32 base32 = new Base32();

    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        RANDOM.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }

    public long currentStep() {
        return Instant.now().getEpochSecond() / PERIOD_SECONDS;
    }

    public String generateCode(String secret, long step) {
        byte[] counter = ByteBuffer.allocate(Long.BYTES).putLong(step).array();
        byte[] hash;
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(base32.decode(secret), HMAC_ALGORITHM));
            hash = mac.doFinal(counter);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
        return String.format("%06d", binary % 1_000_000);
    }

    public OptionalLong verify(String secret, String code) {
        if (code == null) return OptionalLong.empty();
        String normalized = code.replaceAll("\\s", "");
        if (!normalized.matches("\\d{6}")) return OptionalLong.empty();

        byte[] expected = normalized.getBytes(StandardCharsets.US_ASCII);
        long current = currentStep();
        for (long step = current - ALLOWED_DRIFT_STEPS; step <= current + ALLOWED_DRIFT_STEPS; step++) {
            byte[] actual = generateCode(secret, step).getBytes(StandardCharsets.US_ASCII);
            if (MessageDigest.isEqual(actual, expected)) return OptionalLong.of(step);
        }
        return OptionalLong.empty();
    }

}
