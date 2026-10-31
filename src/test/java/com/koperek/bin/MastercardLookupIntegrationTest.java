package com.koperek.bin;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.koperek.api.TransactionResponse;
import com.koperek.api.TriggeredRule;
import com.koperek.risk.RiskLevel;
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

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@WithTestResource(value = WireMockResource.class, scope = TestResourceScope.GLOBAL)
class MastercardLookupIntegrationTest {

    private static final String SEARCH_PATH = "/bin-resources/bin-ranges/account-searches";
    private static final String TRANSACTIONS_ENDPOINT_PATH = "/transactions";
    private static final String REQUEST_ID_HEADER = "X-REQUESTID";
    private static final String REQUEST_ID = "outbound-test-1";
    private static final String PREPAID_USA_RESPONSE = "[{\"country\":{\"alpha3\":\"USA\"},\"fundingSource\":\"PREPAID\"}]";
    private static final String REQUEST_BODY = "{\"bin\":\"411111\",\"amount\":6000,\"countryCode\":\"PL\"}";
    private static final String COUNTRY_MISMATCH_RULE_CODE = "COUNTRY_MISMATCH";
    private static final String HIGH_AMOUNT_RULE_CODE = "HIGH_AMOUNT";
    private static final String PREPAID_CARD_RULE_CODE = "PREPAID_CARD";

    @InjectWireMock
    WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer.resetToDefaultMappings();
    }

    @AfterEach
    void clean() {
        wireMockServer.resetToDefaultMappings();
    }

    @Test
    void forwardsSignedRequestWithRequestIdAndMapsPrepaidCardToVeryHighRisk() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(okJson(PREPAID_USA_RESPONSE)));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .header(REQUEST_ID_HEADER, REQUEST_ID)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        TransactionResponse result = response.as(TransactionResponse.class);
        assertEquals(RiskLevel.VERY_HIGH.score(), result.riskScore());
        assertEquals(RiskLevel.VERY_HIGH.name(), result.riskLevel());
        assertEquals(List.of(COUNTRY_MISMATCH_RULE_CODE, HIGH_AMOUNT_RULE_CODE, PREPAID_CARD_RULE_CODE),
                result.triggeredRules().stream().map(TriggeredRule::rule).toList());
        wireMockServer.verify(postRequestedFor(urlEqualTo(SEARCH_PATH))
                .withHeader("Authorization", matching("OAuth .*oauth_body_hash=.*"))
                .withHeader(REQUEST_ID_HEADER, equalTo(REQUEST_ID))
                .withRequestBody(equalToJson("{\"accountRange\":\"411111\"}")));
    }

    @Test
    void forwardGeneratedRequestIdWhenClientSendsNone() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(okJson(PREPAID_USA_RESPONSE)));

        // when
        Response response = given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH);

        // then
        assertEquals(Status.OK.getStatusCode(), response.statusCode(), response::asString);
        String generatedId = response.header(REQUEST_ID_HEADER);
        assertNotNull(generatedId);
        wireMockServer.verify(postRequestedFor(urlEqualTo(SEARCH_PATH)).withHeader(REQUEST_ID_HEADER, equalTo(generatedId)));
    }
}
