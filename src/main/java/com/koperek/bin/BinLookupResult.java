package com.koperek.bin;

public sealed interface BinLookupResult permits BinLookupResult.Found, BinLookupResult.NotFound, BinLookupResult.Unavailable {
    record Found(BinInfo binInfo) implements BinLookupResult {
    }

    record NotFound() implements BinLookupResult {
    }

    record Unavailable() implements BinLookupResult {
    }
}
