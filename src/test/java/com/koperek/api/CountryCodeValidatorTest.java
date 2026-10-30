package com.koperek.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountryCodeValidatorTest {
    private static final String COUNTRY_CODE_PL = "PL";
    private static final String UNKNOWN_COUNTRY_CODE = "ZZ";
    private final CountryCodeValidator validator = new CountryCodeValidator();

    @Test
    void acceptsKnownCountryCode() {
        // given
        String countryCode = COUNTRY_CODE_PL;

        // when
        boolean valid = validator.isValid(countryCode, null);

        // then
        assertTrue(valid);
    }

    @Test
    void acceptsLowerCaseCountryCode() {
        // given
        String countryCode = COUNTRY_CODE_PL.toLowerCase();

        // when
        boolean valid = validator.isValid(countryCode, null);

        // then
        assertTrue(valid);
    }

    @Test
    void rejectsUnknownCountryCode() {
        // given
        String countryCode = UNKNOWN_COUNTRY_CODE;

        // when
        boolean valid = validator.isValid(countryCode, null);

        // then
        assertFalse(valid);
    }

    @Test
    void acceptsNullCountryCode() {
        // given
        String countryCode = null;

        // when
        boolean valid = validator.isValid(countryCode, null);

        // then
        assertTrue(valid);
    }
}
