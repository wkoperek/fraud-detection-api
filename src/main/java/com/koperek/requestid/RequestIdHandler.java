package com.koperek.requestid;

import io.quarkus.vertx.http.runtime.filters.Filters;
import io.quarkus.vertx.http.runtime.security.SecurityHandlerPriorities;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

@ApplicationScoped
public class RequestIdHandler {
    static final String HEADER = "X-REQUESTID";
    static final String MDC_KEY = "requestId";
    static final String PROPERTY = "requestId";
    static final Pattern VALID_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");
    static final int BEFORE_AUTHENTICATION_PRIORITY = SecurityHandlerPriorities.AUTHENTICATION + 1;


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
