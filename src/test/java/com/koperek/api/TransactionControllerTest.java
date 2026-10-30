package com.koperek.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class TransactionControllerTest {
    private static final String TRANSACTIONS_PATH = "/transactions";
    private static final String HIGH_RISK_LEVEL = "HIGH";

    @Test
    void returnsOkWithRiskAssessmentForValidTransaction() {
        // given
        String body = "{\"bin\":\"411111\",\"amount\":1500.00,\"countryCode\":\"PL\"}";

        // when
        Response response = given().contentType(ContentType.JSON).body(body).post(TRANSACTIONS_PATH);

        // then
        assertEquals(200, response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertEquals(4, result.riskScore());
        assertEquals(HIGH_RISK_LEVEL, result.riskLevel());
    }
}
