package com.koperek.bin.mastercard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koperek.bin.BinInfo;
import com.koperek.bin.BinLookupResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public final class BinInfoFetcher {
    private static final String PREPAID = "PREPAID";
    private static final int NANO_TO_MS = 1_000_000;
    private static final String ACCOUNT_RANGE_FIELD = "accountRange";
    private final ObjectMapper objectMapper;
    private final MastercardBinLookupClient client;

    public BinInfoFetcher(@RestClient MastercardBinLookupClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public BinLookupResult fetch(String bin) {
        return fetchJson(bin)
                .map(this::readBinRanges)
                .filter(binRanges -> !binRanges.isEmpty())
                .map(this::toFound)
                .orElseGet(BinLookupResult.NotFound::new);
    }

    private Optional<String> fetchJson(String bin) {
        try {
            return Optional.of(callMastercard(bin));
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Status.NOT_FOUND.getStatusCode()) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private String callMastercard(String bin) {
        String body = buildRequestBody(bin);

        long start = System.nanoTime();
        String json = client.search(body);
        long durationInMs = (System.nanoTime() - start) / NANO_TO_MS;

        Log.debug("raw json: " + json);
        Log.debug("mastercard lookup durationMs=" + durationInMs);

        return json;
    }

    private String buildRequestBody(String bin) {
        try {
            return objectMapper.writeValueAsString(Map.of(ACCOUNT_RANGE_FIELD, bin));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot build Mastercard request body", e);
        }
    }

    private List<BinRange> readBinRanges(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<BinRange>>() {
            });
        } catch (JsonProcessingException e) {
            throw new InvalidUpstreamResponseException("Cannot parse bin ranges in Mastercard response", e);
        }
    }

    private BinLookupResult toFound(List<BinRange> binRanges) {
        BinRange first = binRanges.getFirst();

        if (haveDifferentCountries(binRanges, first)) {
            Log.warn("Mastercard returned " + binRanges.size() + " bin ranges with different countries, using the first");
        }

        return new BinLookupResult.Found(new BinInfo(first.country().alpha3(), PREPAID.equalsIgnoreCase(first.fundingSource())));
    }

    private boolean haveDifferentCountries(List<BinRange> binRanges, BinRange first) {
        return binRanges.stream().anyMatch(range -> !range.country().alpha3().equals(first.country().alpha3()));
    }

}
