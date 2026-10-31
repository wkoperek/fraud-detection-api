package com.koperek.api;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CurrentRequestId {
    private final RoutingContext routingContext;

    public CurrentRequestId(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

    String value(){
        return routingContext.get(RequestIdHandler.PROPERTY);
    }
}
