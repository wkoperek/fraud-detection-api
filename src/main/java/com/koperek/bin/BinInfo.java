package com.koperek.bin;

import java.util.Optional;

public record BinInfo(Optional<String> countryAlpha3, boolean isPrepaid) {

    public BinInfo(String countryAlpha3, boolean isPrepaid) {
        this(Optional.ofNullable(countryAlpha3), isPrepaid);
    }
}
