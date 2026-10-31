package com.koperek.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class HealthEndpointTest {
    private static final String READY_PATH = "/q/health/ready";

    @Test
    void successfulResponseWithoutAuthentication() {
        // when
        Response response = given().get(READY_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
    }
}
