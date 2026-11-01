package com.koperek.bin.mastercard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record BinRange(String fundingSource, Country country) {

    boolean hasCountry() {
        return country != null && country.alpha3() != null;
    }
}
