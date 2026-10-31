package com.koperek.bin.mastercard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import java.io.IOException;
import org.jboss.logging.MDC;

@ApplicationScoped
public class RequestIdClientFilter implements ClientRequestFilter {
    private static final String MDC_KEY = "requestId";
    private static final String HEADER = "X-REQUESTID";

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Object id = MDC.get(MDC_KEY);
        if (id != null) {
            requestContext.getHeaders().add(HEADER, id.toString());
        }
    }
}
