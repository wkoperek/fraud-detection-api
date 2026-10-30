package com.koperek.security;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.Duration;

import static io.smallrye.jwt.util.KeyUtils.readPrivateKey;
import static java.security.KeyPairGenerator.getInstance;

public final class TestTokens {
    private static final String ISSUER = "https://fraud-detection-api.example/issuer";
    private static final String AUDIENCE = "fraud-detection-api";
    private static final String DEMO_CLIENT = "demo-client";
    private static final String PRIVATE_KEY_LOCATION = "privateKey.pem";
    private static final Duration VALID_LIFETIME = Duration.ofMinutes(5);
    private static final int RSA_KEY_SIZE = 2048;

    private TestTokens() {
    }

    public static String valid() {
        return validClaims().sign(privateKey());
    }

    public static String signedWithOtherKey() {
        return validClaims().sign(freshPrivateKey());
    }

    private static JwtClaimsBuilder validClaims() {
        return Jwt.issuer(ISSUER)
                .subject(DEMO_CLIENT)
                .upn(DEMO_CLIENT)
                .audience(AUDIENCE)
                .expiresIn(VALID_LIFETIME);
    }

    private static PrivateKey freshPrivateKey() {
        try {
            KeyPairGenerator generator = getInstance("RSA");
            generator.initialize(RSA_KEY_SIZE);
            return generator.generateKeyPair().getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA is not available", e);
        }
    }

    private static PrivateKey privateKey() {
        try {
            return readPrivateKey(PRIVATE_KEY_LOCATION);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Cannot read test private key", e);
        }
    }
}