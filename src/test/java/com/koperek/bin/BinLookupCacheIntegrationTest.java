package com.koperek.bin;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.koperek.api.TransactionResponse;
import com.koperek.api.TriggeredRule;
import com.koperek.security.TestTokens;
import com.koperek.support.InjectWireMock;
import com.koperek.support.WireMockResource;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@WithTestResource(value = WireMockResource.class, scope = TestResourceScope.GLOBAL)
class BinLookupCacheIntegrationTest {
    private static final String REQUEST_BODY = "{\"bin\":\"411111\",\"amount\":100,\"countryCode\":\"PL\"}";
    private static final String SEARCH_PATH = "/bin-resources/bin-ranges/account-searches";
    private static final String TRANSACTIONS_ENDPOINT_PATH = "/transactions";
    private static final String USA_DEBIT_RESPONSE = "[{\"country\":{\"alpha3\":\"USA\"},\"fundingSource\":\"DEBIT\"}]";
    private static final String BIN_NOT_VERIFIED_RULE_CODE = "BIN_NOT_VERIFIED";
    private static final String COUNTRY_MISMATCH_RULE_CODE = "COUNTRY_MISMATCH";

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
    void doesNotCacheFailureAndServesLaterSuccessFromCache() {
        // given
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(serverError()));
        TransactionResponse afterFailure = postTransaction();
        wireMockServer.stubFor(post(urlEqualTo(SEARCH_PATH)).willReturn(okJson(USA_DEBIT_RESPONSE)));

        // when
        TransactionResponse afterRecovery = postTransaction();
        TransactionResponse fromCache = postTransaction();

        // then
        assertTrue(afterFailure.degraded());
        assertEquals(List.of(BIN_NOT_VERIFIED_RULE_CODE), ruleCodes(afterFailure));
        assertFalse(afterRecovery.degraded());
        assertEquals(List.of(COUNTRY_MISMATCH_RULE_CODE), ruleCodes(afterRecovery));
        assertEquals(List.of(COUNTRY_MISMATCH_RULE_CODE), ruleCodes(fromCache));
        wireMockServer.verify(2, postRequestedFor(urlEqualTo(SEARCH_PATH)));
    }

    private TransactionResponse postTransaction() {
        return given().auth().oauth2(TestTokens.valid())
                .contentType(ContentType.JSON)
                .body(REQUEST_BODY)
                .post(TRANSACTIONS_ENDPOINT_PATH)
                .then().statusCode(200)
                .extract().as(TransactionResponse.class);
    }

    private static List<String> ruleCodes(TransactionResponse response) {
        return response.triggeredRules().stream().map(TriggeredRule::rule).toList();
    }
}