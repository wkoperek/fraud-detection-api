package com.koperek.api;

import io.quarkus.logging.Log;
import io.quarkus.security.spi.runtime.AuthenticationFailureEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class AuthFailureLogger {

    void onAuthenticationFailure(@Observes AuthenticationFailureEvent event) {
        Log.warnf("authentication failed: %s", event.getAuthenticationFailure().getClass().getName());
    }
}
