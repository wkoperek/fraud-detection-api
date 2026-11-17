package com.koperek.bin.mastercard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.developer.oauth.OAuth;
import com.mastercard.developer.utils.AuthenticationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;


@ApplicationScoped
public class MastercardOAuthFilter implements ClientRequestFilter {
    private final String consumerKey;
    private final PrivateKey signingKey;
    private final ObjectMapper objectMapper;

    public MastercardOAuthFilter(MastercardConfig config, ObjectMapper objectMapper) {
        this.consumerKey = config.consumerKey();
        this.signingKey = loadSigningKey(config);
        this.objectMapper = objectMapper;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String payload = toPayload(requestContext.getEntity());
        String header = OAuth.getAuthorizationHeader(requestContext.getUri(), requestContext.getMethod(), payload, StandardCharsets.UTF_8, consumerKey, signingKey);
        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, header);
    }

    // Signs the same JSON the client sends: both are written by the shared ObjectMapper.
    private String toPayload(Object entity) throws IOException {
        if (entity == null) {
            return null;
        }
        if (entity instanceof String body) {
            return body;
        }
        return objectMapper.writeValueAsString(entity);
    }

    private static PrivateKey loadSigningKey(MastercardConfig config) {
        try {
            return AuthenticationUtils.loadSigningKey(config.keystorePath(), config.keyAlias(), config.keystorePassword());
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Cannot load Mastercard signing key", e);
        }
    }
}
