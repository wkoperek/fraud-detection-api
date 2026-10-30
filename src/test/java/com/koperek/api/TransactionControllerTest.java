package com.koperek.api;

import com.koperek.risk.RiskLevel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.Response.Status;

import java.util.List;

@QuarkusTest
class TransactionControllerTest {
    private static final String TRANSACTIONS_PATH = "/transactions";
    private static final String HIGH_RISK_LEVEL = "HIGH";
    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String BIN_FIELD_NAME = "bin";
    private static final String BIN_PATTERN_VIOLATION_MSG = "must match \"^\\d{6,8}$\"";
    private static final String CORRECT_REQ_BODY_JSON = "{\"bin\":\"411111\",\"amount\":1500.00,\"countryCode\":\"PL\"}";
    private static final String TOO_SHORT_BIN_REQ_BODY_EXAMPLE = "{\"bin\":\"12345\",\"amount\":1500.00,\"countryCode\":\"PL\"}";


    @Test
    void returnsOkWithRiskAssessmentForValidTransaction() {
        // given
        String body = CORRECT_REQ_BODY_JSON;

        // when
        Response response = given().contentType(ContentType.JSON).body(body).post(TRANSACTIONS_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertEquals(RiskLevel.HIGH.score(), result.riskScore());
        assertEquals(HIGH_RISK_LEVEL, result.riskLevel());
    }

    @Test
    void returnsBadRequestForTooShortBin() {
        // given
        String body = TOO_SHORT_BIN_REQ_BODY_EXAMPLE;

        // when
        Response response = given().contentType(ContentType.JSON).body(body).post(TRANSACTIONS_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(VALIDATION_FAILED, error.error());
        assertEquals(List.of(new ErrorResponse.Violation(BIN_FIELD_NAME, BIN_PATTERN_VIOLATION_MSG)), error.violations());
    }
}
