package com.koperek.bin.mastercard;

import com.koperek.bin.BinLookupResult;
import com.koperek.bin.BinLookupService;
import com.koperek.bin.UpstreamRejectedException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;

@ApplicationScoped
public class MastercardBinLookupService implements BinLookupService {

    private final BinInfoFetcher fetcher;

    public MastercardBinLookupService(BinInfoFetcher fetcher) {
        this.fetcher = fetcher;
    }

    private static boolean isTransient(int status) {
        return Family.familyOf(status) == Family.SERVER_ERROR
                || status == Status.TOO_MANY_REQUESTS.getStatusCode();
    }

    private static BinLookupResult unavailable(String reasonFormat, Object... args) {
        Log.warnf("BIN lookup failed, " + reasonFormat, args);
        return new BinLookupResult.Unavailable();
    }

    @Override
    public BinLookupResult lookup(String bin) {
        try {
            return fetcher.fetch(bin);
        } catch (ProcessingException e) {
            Throwable reason = e.getCause() != null ? e.getCause() : e;
            return unavailable("transport error=%s", reason.getClass().getSimpleName());
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (!isTransient(status)) {
                throw new UpstreamRejectedException(status, e);
            }
            return unavailable("upstream status=%d", status);
        } catch (InvalidUpstreamResponseException e) {
            return unavailable("invalid upstream response=%s", e.getCause().getClass().getSimpleName());
        }
    }
}
