package com.koperek.security;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.KeyUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.time.Duration;

public final class TestTokens {
    private static final String ISSUER = "https://fraud-detection-api.example/issuer";
    private static final String AUDIENCE = "fraud-detection-api";
    private static final String DEMO_CLIENT = "demo-client";
    private static final String PRIVATE_KEY_LOCATION = "privateKey.pem";
    private static final Duration VALID_LIFETIME = Duration.ofMinutes(5);

    private TestTokens() {
    }

    public static String valid() {
        return Jwt.issuer(ISSUER)
                .subject(DEMO_CLIENT)
                .upn(DEMO_CLIENT)
                .audience(AUDIENCE)
                .expiresIn(VALID_LIFETIME)
                .sign(privateKey());
    }

    private static PrivateKey privateKey() {
        try {
            return KeyUtils.readPrivateKey(PRIVATE_KEY_LOCATION);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Cannot read test private key", e);
        }
    }
}