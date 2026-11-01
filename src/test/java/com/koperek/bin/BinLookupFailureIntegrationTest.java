package com.koperek.bin;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.koperek.api.ErrorResponse;
import com.koperek.api.TransactionResponse;
import com.koperek.api.TriggeredRule;
import com.koperek.security.TestTokens;
import com.koperek.support.InjectWireMock;
import com.koperek.support.WireMockResource;
import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import jakarta.inject.Inject;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@WithTestResource(value = WireMockResource.class, scope = TestResourceScope.GLOBAL)
class BinLookupFailureIntegrationTest {
    private static final String REQUEST_BODY = "{\"bin\":\"411111\",\"amount\":6000,\"countryCode\":\"PL\"}";
    private static final String SEARCH_PATH = "/bin-resources/bin-ranges/account-searches";
    private static final String TRANSACTIONS_ENDPOINT_PATH = "/transactions";
    private static final String BIN_NOT_VERIFIED_TRIGGER_CODE = "BIN_NOT_VERIFIED";
    private static final String HIGH_AMOUNT_TRIGGER_CODE = "HIGH_AMOUNT";
    private static final String UPSTREAM_REJECTED_ERROR_CODE = "UPSTREAM_REJECTED";
    private static final String X_REQUESTID_HEADER = "X-REQUESTID";
    private static final String FUNDING_SOURCE_PREPAID_JSON = "[{\"fundingSource\":\"PREPAID\"}]";

    @InjectWireMock
    WireMockServer wireMockServer;

    @Inject
    @CacheName("bin-lookup")
    Cache binLookupCache;

    @BeforeEach
    void setup() {
        binLookupCache.invalidateAll().await().indefinitely();
        wireMockServer.resetToDefaultMappings();
    }

    @AfterEach
    void clean() {
        binLookupCache.invalidateAll().await().indefinitely();
        wireMockServer.resetToDefaultMappings();
    }

    @Test
    void degradesWhenUpstreamReturnsServerError() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(serverError()));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertTrue(result.degraded());
        assertEquals(List.of(BIN_NOT_VERIFIED_TRIGGER_CODE, HIGH_AMOUNT_TRIGGER_CODE),
                result.triggeredRules().stream().map(TriggeredRule::rule).toList());
    }

    @Test
    void degradesWhenUpstreamExceedsReadTimeout() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(ok().withFixedDelay(1500)));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertTrue(result.degraded());
        assertEquals(List.of(BIN_NOT_VERIFIED_TRIGGER_CODE, HIGH_AMOUNT_TRIGGER_CODE),
                result.triggeredRules().stream().map(TriggeredRule::rule).toList());
    }

    @Test
    void degradesWhenUpstreamReturnsUnexpectedJsonShape() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(okJson("\"not-json\"")));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertTrue(result.degraded());
        assertEquals(List.of(BIN_NOT_VERIFIED_TRIGGER_CODE, HIGH_AMOUNT_TRIGGER_CODE),
                result.triggeredRules().stream().map(TriggeredRule::rule).toList());
    }

    @Test
    void scoresAsNotVerifiedWhenUpstreamReturnsEmptyArray() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(okJson("[]")));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);
        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertFalse(result.degraded());
        assertEquals(List.of(BIN_NOT_VERIFIED_TRIGGER_CODE, HIGH_AMOUNT_TRIGGER_CODE),
                result.triggeredRules().stream().map(TriggeredRule::rule).toList());
    }

    @Test
    void scoresAsNotVerifiedWhenUpstreamReturnsNotFound() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(notFound()));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertFalse(result.degraded());
        assertEquals(List.of(BIN_NOT_VERIFIED_TRIGGER_CODE, HIGH_AMOUNT_TRIGGER_CODE),
                result.triggeredRules().stream().map(TriggeredRule::rule).toList());
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void respondsWithBadGatewayWhenUpstreamRejectsCredentials(int upstreamStatus) {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(aResponse().withStatus(upstreamStatus)));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.BAD_GATEWAY.getStatusCode(), response.statusCode(), response::asString);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertEquals(UPSTREAM_REJECTED_ERROR_CODE, error.error());
        assertNotNull(error.requestId());
        assertEquals(error.requestId(), response.header(X_REQUESTID_HEADER));
    }

    @Test
    void scoresAsNotVerifiedWhenUpstreamRangeHasNoCountry() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(okJson(FUNDING_SOURCE_PREPAID_JSON)));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertFalse(result.degraded());
        assertEquals(List.of(BIN_NOT_VERIFIED_TRIGGER_CODE, HIGH_AMOUNT_TRIGGER_CODE),
                result.triggeredRules().stream().map(TriggeredRule::rule).toList());
    }

}
