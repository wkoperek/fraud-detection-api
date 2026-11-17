package com.koperek.bin.mastercard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.koperek.bin.BinInfo;
import com.koperek.bin.BinLookupResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import io.quarkus.cache.CacheResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class BinInfoFetcher {
    private static final String PREPAID = "PREPAID";
    private static final int NANO_TO_MS = 1_000_000;
    private final MastercardBinLookupClient client;

    public BinInfoFetcher(@RestClient MastercardBinLookupClient client) {
        this.client = client;
    }

    // Never catch failures here: a returned fallback would be cached. MastercardBinLookupService handles them.
    @CacheResult(cacheName = "bin-lookup")
    public BinLookupResult fetch(String bin) {
        return fetchBinRanges(bin)
                .map(this::withoutNulls)
                .filter(binRanges -> !binRanges.isEmpty())
                .map(this::toFound)
                .orElseGet(BinLookupResult.NotFound::new);
    }

    private Optional<List<BinRange>> fetchBinRanges(String bin) {
        try {
            return Optional.ofNullable(callMastercard(bin));
        } catch (WebApplicationException e) {
            if (e.getCause() instanceof JsonProcessingException cause) {
                throw invalidResponse(cause);
            }
            if (e.getResponse().getStatus() == Status.NOT_FOUND.getStatusCode()) {
                return Optional.empty();
            }
            throw e;
        } catch (ProcessingException e) {
            if (e.getCause() instanceof JsonProcessingException cause) {
                throw invalidResponse(cause);
            }
            throw e;
        }
    }

    private List<BinRange> callMastercard(String bin) {
        long start = System.nanoTime();
        List<BinRange> binRanges = client.search(new BinRangeSearchRequest(bin));
        long durationInMs = (System.nanoTime() - start) / NANO_TO_MS;

        Log.debug("mastercard response: " + binRanges);
        Log.debug("mastercard lookup durationMs=" + durationInMs);

        return binRanges;
    }

    private static InvalidUpstreamResponseException invalidResponse(JsonProcessingException cause) {
        return new InvalidUpstreamResponseException("Cannot parse bin ranges in Mastercard response", cause);
    }

    private List<BinRange> withoutNulls(List<BinRange> binRanges) {
        return binRanges.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    // Prefers a range with a country, but keeps the funding source of a range without one.
    private BinLookupResult toFound(List<BinRange> binRanges) {
        BinRange chosen = binRanges.stream()
                .filter(BinRange::hasCountry)
                .findFirst()
                .orElse(binRanges.getFirst());

        if (haveDifferentCountries(binRanges, chosen)) {
            Log.warn("Mastercard returned " + binRanges.size() + " bin ranges with different countries, using " + chosen.countryAlpha3().orElse("none"));
        }

        return new BinLookupResult.Found(new BinInfo(chosen.countryAlpha3(), PREPAID.equalsIgnoreCase(chosen.fundingSource())));
    }

    private boolean haveDifferentCountries(List<BinRange> binRanges, BinRange chosen) {
        return binRanges.stream()
                .filter(BinRange::hasCountry)
                .anyMatch(range -> !range.countryAlpha3().equals(chosen.countryAlpha3()));
    }

}
