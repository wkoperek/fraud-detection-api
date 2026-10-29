package com.koperek.bin;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class StubLookupService implements BinLookupService {

    private static final Map<String, BinInfo> BINS = Map.of(
            "411111", new BinInfo("USA", false),
            "511111", new BinInfo("USA", true),
            "520000", new BinInfo("POL", true),
            "222100", new BinInfo("DEU", false));


    @Override
    public BinLookupResult lookUp(String bin) {
        BinInfo binInfo = BINS.get(bin);
        if (binInfo == null) {
            return new BinLookupResult.NotFound();
        } else {
            return new BinLookupResult.Found(binInfo);
        }
    }
}
