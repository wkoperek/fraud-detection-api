package com.koperek.requestid;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CurrentRequestId {
    private final RoutingContext routingContext;

    public CurrentRequestId(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

    public String value() {
        return routingContext.get(RequestIdHandler.PROPERTY);
    }
}
