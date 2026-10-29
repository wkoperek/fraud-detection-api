package com.koperek.risk;

import com.koperek.bin.BinInfo;
import com.koperek.bin.BinLookupResult;
import com.koperek.risk.rules.CountryMismatchRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountryMismatchRuleTest {

    private static final String COUNTRY_CODE_PL = "PL";
    private static final String COUNTRY_ALPHA_3_POL = "POL";
    private static final String COUNTRY_ALPHA_3_USA = "USA";
    private final CountryMismatchRule rule = new CountryMismatchRule();

    @Test
    void returnsEmptyWhenCountriesMatch() {
        // given
        Transaction transaction = transactionIn(COUNTRY_CODE_PL);
        BinLookupResult binLookupResult = cardIssuedIn(COUNTRY_ALPHA_3_POL);

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, binLookupResult);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsTwoPointsWhenCountriesDiffer() {
        // given
        Transaction transaction = transactionIn(COUNTRY_CODE_PL);
        BinLookupResult binLookupResult = cardIssuedIn(COUNTRY_ALPHA_3_USA);

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, binLookupResult);

        // then
        assertTrue(result.isPresent());
        assertEquals(CountryMismatchRule.NAME, result.get().rule());
        assertEquals(2, result.get().points());
        assertEquals("Card issued in USA, transaction in POL", result.get().reason());
    }

    @Test
    void ignoresLetterCaseOfTransactionCountry() {
        // given
        Transaction transaction = transactionIn(COUNTRY_CODE_PL.toLowerCase());
        BinLookupResult binLookupResult = cardIssuedIn(COUNTRY_ALPHA_3_POL);

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, binLookupResult);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void ignoresLetterCaseOfCardCountry() {
        // given
        Transaction transaction = transactionIn(COUNTRY_CODE_PL);
        BinLookupResult binLookupResult = cardIssuedIn(COUNTRY_ALPHA_3_POL.toLowerCase());

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, binLookupResult);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenBinNotFound() {
        // given
        Transaction transaction = transactionIn(COUNTRY_CODE_PL);
        BinLookupResult binLookupResult = new BinLookupResult.NotFound();

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, binLookupResult);

        // then
        assertTrue(result.isEmpty());
    }

    private static Transaction transactionIn(String countryCode) {
        return new Transaction("411111", new BigDecimal("100.00"), countryCode);
    }

    private static BinLookupResult cardIssuedIn(String countryAlpha3) {
        return new BinLookupResult.Found(new BinInfo(countryAlpha3, false));
    }
}