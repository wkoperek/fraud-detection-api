package com.koperek.api;

import com.koperek.security.TestTokens;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class RequestIdTest {
    private static final String TRANSACTIONS_ENDPOINT_PATH = "/transactions";
    private static final String CORRECT_REQ_BODY_JSON = "{\"bin\":\"411111\",\"amount\":1500.00,\"countryCode\":\"PL\"}";
    private static final String TOO_SHORT_BIN_REQ_BODY_EXAMPLE = "{\"bin\":\"12345\",\"amount\":1500.00,\"countryCode\":\"PL\"}";
    private static final String REQUEST_ID_HEADER = "X-REQUESTID";

    @Test
    void generatesRequestIdWhenHeaderIsMissing() {
        // given
        String body = CORRECT_REQ_BODY_JSON;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        String headerId = response.header(REQUEST_ID_HEADER);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        UUID.fromString(headerId); // if requestId from header was not generated UUID.fromString will fail
        assertEquals(headerId, response.as(TransactionResponse.class).requestId());
    }

    @Test
    void propagatesSuppliedRequestIdWhenHeaderIsPreset() {
        // given
        String body = CORRECT_REQ_BODY_JSON;
        String suppliedRequestId = UUID.randomUUID().toString();

        // when
        Response response = authorizedJsonRequest().body(body).header(REQUEST_ID_HEADER, suppliedRequestId)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        String headerId = response.header(REQUEST_ID_HEADER);
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(headerId, response.as(TransactionResponse.class).requestId());
        assertEquals(suppliedRequestId, headerId);
    }

    @Test
    void addsRequestIdHeaderToUnauthorizedResponse() {
        // given
        String body = CORRECT_REQ_BODY_JSON;
        String suppliedRequestId = UUID.randomUUID().toString();

        // when
        Response response = given().contentType(ContentType.JSON).body(body).header(REQUEST_ID_HEADER, suppliedRequestId)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.statusCode());
        assertEquals(suppliedRequestId, response.header(REQUEST_ID_HEADER));
    }

    @Test
    void includesRequestIdInErrorResponse() {
        // given
        String body = TOO_SHORT_BIN_REQ_BODY_EXAMPLE;
        String suppliedRequestId = UUID.randomUUID().toString();

        // when
        Response response = authorizedJsonRequest().body(body).header(REQUEST_ID_HEADER, suppliedRequestId)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode());
        assertEquals(suppliedRequestId, response.as(ErrorResponse.class).requestId());
    }

    private static RequestSpecification authorizedJsonRequest() {
        return given().auth().oauth2(TestTokens.valid()).contentType(ContentType.JSON);
    }
}
