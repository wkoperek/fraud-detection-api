package com.koperek.bin.mastercard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
record BinRange(String fundingSource, Country country) {

    boolean hasCountry() {
        return countryAlpha3().isPresent();
    }

    Optional<String> countryAlpha3() {
        return Optional.ofNullable(country).map(Country::alpha3);
    }
}
