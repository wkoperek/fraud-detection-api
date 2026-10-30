package com.koperek.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test/fail")
@Produces(MediaType.APPLICATION_JSON)
public class FailingResourceTest {
    private static final String FAILING_CONTROLLER_TEST_RESPONSE_ERROR = "boom";

    @GET
    public String fail() {
        throw new IllegalStateException(FAILING_CONTROLLER_TEST_RESPONSE_ERROR);
    }
}
