package com.koperek.requestid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import java.io.IOException;

import org.jboss.logging.MDC;

@ApplicationScoped
public class RequestIdClientFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Object id = MDC.get(RequestIdHandler.MDC_KEY);
        if (id != null) {
            requestContext.getHeaders().add(RequestIdHandler.HEADER, id.toString());
        }
    }
}
