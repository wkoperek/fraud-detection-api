package com.koperek.security;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class TransactionControllerAuthTest {
    private static final String TRANSACTIONS_ENDPOINT_PATH = "/transactions";
    private static final String CORRECT_REQ_BODY_JSON = "{\"bin\":\"411111\",\"amount\":1500.00,\"countryCode\":\"PL\"}";

    @Test
    void returnsUnauthorizedWithoutToken() {
        // given
        String body = CORRECT_REQ_BODY_JSON;

        // when
        Response response = given().contentType(ContentType.JSON).body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(jakarta.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode(), response.statusCode(), response::asString);
    }
}
