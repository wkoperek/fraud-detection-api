package com.koperek.api;

import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

@ApplicationScoped
public class RequestIdHandler {
    public static final String HEADER = "X-REQUESTID";
    public static final String MDC_KEY = "requestId";
    public static final String PROPERTY = "requestId";
    public static final Pattern VALID_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");
    public static final int BEFORE_AUTHENTICATION_PRIORITY = 250;


    void register(@Observes Filters filters) {
        filters.register(this::handleOutgoingIdFilter, BEFORE_AUTHENTICATION_PRIORITY);
    }

    private void handleOutgoingIdFilter(RoutingContext routingContext) {
        String outgoingId = resolveOutgoingId(routingContext);

        routingContext.response().putHeader(HEADER, outgoingId);
        routingContext.put(PROPERTY, outgoingId);
        MDC.put(MDC_KEY, outgoingId);
        routingContext.addEndHandler(result -> MDC.remove(MDC_KEY));
        routingContext.next();
    }

    private String resolveOutgoingId(RoutingContext routingContext) {
        String headerValue = routingContext.request().getHeader(HEADER);
        return isValidOutgoingId(headerValue) ? headerValue : generateOutgoingId();
    }

    private boolean isValidOutgoingId(String value) {
        return value != null && VALID_PATTERN.matcher(value).matches();
    }

    private String generateOutgoingId() {
        return UUID.randomUUID().toString();
    }
}
