package com.koperek.bin.mastercard;

import com.koperek.bin.BinLookupResult;
import com.koperek.bin.BinLookupService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MastercardBinLookupService implements BinLookupService {

    private final BinInfoFetcher fetcher;

    public MastercardBinLookupService(BinInfoFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public BinLookupResult lookup(String bin) {
        return fetcher.fetch(bin);
    }
}
