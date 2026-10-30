package com.koperek.api;

import com.koperek.risk.RiskLevel;
import com.koperek.security.TestTokens;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.ws.rs.core.Response.Status;

import java.util.List;

@QuarkusTest
class TransactionControllerTest {
    private static final String TRANSACTIONS_ENDPOINT_PATH = "/transactions";
    private static final String HIGH_RISK_LEVEL = "HIGH";
    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String BIN_FIELD_NAME = "bin";
    private static final String UNKNOWN_ENDPOINT_PATH = "/unknown";
    private static final String NOT_FOUND_ERROR_CODE = "NOT_FOUND";
    private static final String NOT_FOUND_ERROR_MSG = "Not Found";
    private static final String BIN_PATTERN_VIOLATION_MSG = "must match \"^\\d{6,8}$\"";
    private static final String CORRECT_REQ_BODY_JSON = "{\"bin\":\"411111\",\"amount\":1500.00,\"countryCode\":\"PL\"}";
    private static final String TOO_SHORT_BIN_REQ_BODY_EXAMPLE = "{\"bin\":\"12345\",\"amount\":1500.00,\"countryCode\":\"PL\"}";
    private static final String NEGATIVE_AMOUNT_REQ_BODY_EXAMPLE = "{\"bin\":\"411111\",\"amount\":-1500.00,\"countryCode\":\"PL\"}";
    private static final String MISSING_COUNTRY_CODE_REQ_BODY_EXAMPLE = "{\"bin\":\"411111\",\"amount\":1500.00}";
    private static final String UNKNOWN_COUNTRY_CODE_REQ_BODY_EXAMPLE = "{\"bin\":\"411111\",\"amount\":1500.00,\"countryCode\":\"ZZ\"}";
    private static final String MALFORMED_REQ_BODY_JSON = "{\"bin\":\"411111\",\"amount\":1500.00";
    private static final String AMOUNT_FIELD_NAME = "amount";
    private static final String MUST_BE_GREATER_THAN_0_VIOLATION_MSG = "must be greater than 0";
    private static final String COUNTRY_CODE_FIELD_NAME = "countryCode";
    private static final String MUST_NOT_BE_BLANK_ERROR_MSG = "must not be blank";
    private static final String ISO_3166_1_ALPHA_2_COUNTRY_CODE_ERROR_MSG = "Must be a valid ISO 3166-1 alpha-2 country code";
    private static final String MALFORMED_REQUEST_ERROR_CODE = "MALFORMED_REQUEST";
    private static final String REQUEST_BODY_IS_MALFORMED_ERROR_MSG = "Request body is malformed";
    private static final String WRONG_TYPE_REQ_BODY_JSON = "{\"bin\":\"411111\",\"amount\":\"abc\",\"countryCode\":\"PL\"}";
    private static final String REQUEST_FIELD_NAME = "request";
    private static final String MUST_NOT_BE_NULL_ERROR_MSG = "must not be null";
    private static final String METHOD_NOT_ALLOWED_ERROR_CODE = "METHOD_NOT_ALLOWED";
    private static final String METHOD_NOT_ALLOWED_ERROR_MSG = "Method Not Allowed";
    private static final String UNSUPPORTED_MEDIA_TYPE_ERROR_CODE = "UNSUPPORTED_MEDIA_TYPE";
    private static final String UNSUPPORTED_MEDIA_TYPE_ERROR_MSG = "Unsupported Media Type";
    private static final String FAILING_ENDPOINT_PATH = "/test/fail";
    private static final String INTERNAL_ERROR_CODE = "INTERNAL_ERROR";
    private static final String INTERNAL_SERVER_ERROR_MSG = "Internal server error";
    private static final String FAILING_CONTROLLER_TEST_RESPONSE_ERROR = "boom";
    private static final String COUNTRY_MISMATCH_RULE = "COUNTRY_MISMATCH";
    private static final String HIGH_AMOUNT_RULE = "HIGH_AMOUNT";


    @Test
    void returnsOkWithRiskAssessmentForValidTransaction() {
        // given
        String body = CORRECT_REQ_BODY_JSON;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertEquals(RiskLevel.HIGH.score(), result.riskScore());
        assertEquals(List.of(COUNTRY_MISMATCH_RULE, HIGH_AMOUNT_RULE), result.triggeredRules().stream().map(TriggeredRule::rule).toList());
    }

    @Test
    void returnsBadRequestForTooShortBin() {
        // given
        String body = TOO_SHORT_BIN_REQ_BODY_EXAMPLE;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(VALIDATION_FAILED, error.error());
        assertEquals(List.of(new ErrorResponse.Violation(BIN_FIELD_NAME, BIN_PATTERN_VIOLATION_MSG)), error.violations());
    }

    @Test
    void returnsBadRequestForNegativeAmount() {
        // given
        String body = NEGATIVE_AMOUNT_REQ_BODY_EXAMPLE;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(VALIDATION_FAILED, error.error());
        assertEquals(List.of(new ErrorResponse.Violation(AMOUNT_FIELD_NAME, MUST_BE_GREATER_THAN_0_VIOLATION_MSG)), error.violations());
    }

    @Test
    void returnsBadRequestForMissingCountryCode() {
        // given
        String body = MISSING_COUNTRY_CODE_REQ_BODY_EXAMPLE;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(VALIDATION_FAILED, error.error());
        assertEquals(List.of(new ErrorResponse.Violation(COUNTRY_CODE_FIELD_NAME, MUST_NOT_BE_BLANK_ERROR_MSG)), error.violations());
    }

    @Test
    void returnsBadRequestForUnknownCountryCode() {
        // given
        String body = UNKNOWN_COUNTRY_CODE_REQ_BODY_EXAMPLE;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(VALIDATION_FAILED, error.error());
        assertEquals(List.of(new ErrorResponse.Violation(COUNTRY_CODE_FIELD_NAME, ISO_3166_1_ALPHA_2_COUNTRY_CODE_ERROR_MSG)), error.violations());
    }

    @Test
    void returnsBadRequestForMalformedJson() {
        // given
        String body = MALFORMED_REQ_BODY_JSON;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(MALFORMED_REQUEST_ERROR_CODE, error.error());
        assertEquals(REQUEST_BODY_IS_MALFORMED_ERROR_MSG, error.message());
    }

    @Test
    void returnsBadRequestForWrongAmountType() {
        // given
        String body = WRONG_TYPE_REQ_BODY_JSON;

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(MALFORMED_REQUEST_ERROR_CODE, error.error());
        assertEquals(REQUEST_BODY_IS_MALFORMED_ERROR_MSG, error.message());
    }

    @Test
    void returnsBadRequestForEmptyBody() {
        // given
        String body = "";

        // when
        Response response = authorizedJsonRequest().body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(VALIDATION_FAILED, error.error());
        assertEquals(List.of(new ErrorResponse.Violation(REQUEST_FIELD_NAME, MUST_NOT_BE_NULL_ERROR_MSG)), error.violations());
    }

    @Test
    void returnsMethodNotAllowedForGet() {
        // given
        String path = TRANSACTIONS_ENDPOINT_PATH;

        // when
        Response response = given().get(path);

        // then
        assertEquals(Status.METHOD_NOT_ALLOWED.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(METHOD_NOT_ALLOWED_ERROR_CODE, error.error());
        assertEquals(METHOD_NOT_ALLOWED_ERROR_MSG, error.message());
    }

    @Test
    void returnsUnsupportedMediaTypeForTextPlain() {
        // given
        String body = CORRECT_REQ_BODY_JSON;

        // when
        Response response = given().contentType(ContentType.TEXT).body(body).post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(UNSUPPORTED_MEDIA_TYPE_ERROR_CODE, error.error());
        assertEquals(UNSUPPORTED_MEDIA_TYPE_ERROR_MSG, error.message());
    }

    @Test
    void returnsNotFoundForUnknownPath() {
        // given
        String path = UNKNOWN_ENDPOINT_PATH;

        // when
        Response response = given().get(path);

        // then
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(NOT_FOUND_ERROR_CODE, error.error());
        assertEquals(NOT_FOUND_ERROR_MSG, error.message());
    }

    @Test
    void returnsInternalServerErrorWithoutLeakingDetails() {
        // given
        String path = FAILING_ENDPOINT_PATH;

        // when
        Response response = given().get(path);

        // then
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(INTERNAL_ERROR_CODE, error.error());
        assertEquals(INTERNAL_SERVER_ERROR_MSG, error.message());
        assertFalse(response.asString().contains(FAILING_CONTROLLER_TEST_RESPONSE_ERROR));
    }

    private static RequestSpecification authorizedJsonRequest() {
        return given().auth().oauth2(TestTokens.valid()).contentType(ContentType.JSON);
    }
}
