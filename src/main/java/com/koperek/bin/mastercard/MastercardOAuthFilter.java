package com.koperek.bin.mastercard;

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

    public MastercardOAuthFilter(MastercardConfig config) {
        this.consumerKey = config.consumerKey();
        this.signingKey = loadSigningKey(config);
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String payload = (String) requestContext.getEntity();
        String header = OAuth.getAuthorizationHeader(requestContext.getUri(), requestContext.getMethod(), payload, StandardCharsets.UTF_8, consumerKey, signingKey);
        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, header);
    }

    private static PrivateKey loadSigningKey(MastercardConfig config) {
        try {
            return AuthenticationUtils.loadSigningKey(config.keystorePath(), config.keyAlias(), config.keystorePassword());
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Cannot load Mastercard signing key", e);
        }
    }
}
