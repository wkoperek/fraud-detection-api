package com.koperek.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class WireMockResource implements QuarkusTestResourceLifecycleManager {
    private static final String WIREMOCK_CLASSPATH = "wiremock";
    private static final String CONSUMER_KEY = "test-consumer-key";
    private static final String KEYSTORE_PATH = "src/test/resources/test-signing.p12";
    private static final String KEYSTORE_PASSWORD = "teststorepass";
    private static final String KEY_ALIAS = "test-key";
    private static final String READ_TIMEOUT_MS = "500";

    private WireMockServer server;

    @Override
    public Map<String, String> start() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().usingFilesUnderClasspath(WIREMOCK_CLASSPATH));
        server.start();

        return Map.of(
                "quarkus.rest-client.bin-lookup.url", server.baseUrl() + "/bin-resources",
                "mastercard.consumer-key", CONSUMER_KEY,
                "mastercard.keystore-path", KEYSTORE_PATH,
                "mastercard.keystore-password", KEYSTORE_PASSWORD,
                "mastercard.key-alias", KEY_ALIAS,
                "quarkus.rest-client.bin-lookup.read-timeout", READ_TIMEOUT_MS);
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public void inject(TestInjector injector) {
        injector.injectIntoFields(server, new TestInjector.AnnotatedAndMatchesType(InjectWireMock.class, WireMockServer.class));
    }
}
