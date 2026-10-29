package com.koperek.risk.rules;

import com.koperek.bin.BinLookupResult;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HighAmountRuleTest {
    public static final String COUNTRY_CODE_PL = "PL";
    private final HighAmountRule rule = new HighAmountRule(new BigDecimal("1000"), new BigDecimal("5000"));

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "999.99"})
    void returnsEmptyBelowMediumThreshold(String amount) {
        // given
        Transaction transaction = transactionWithAmount(amount);

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, new BinLookupResult.NotFound());

        // then
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"1000, 1", "1000.00, 1", "4999.99, 1", "5000, 2", "1000000, 2"})
    void returnsPointsAboveThreshold(String amount, int expectedPoints) {
        // given
        Transaction transaction = transactionWithAmount(amount);

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, new BinLookupResult.NotFound());

        // then
        assertTrue(result.isPresent());
        assertEquals(expectedPoints, result.get().points());
    }

    @ParameterizedTest
    @CsvSource({"6000.00, Amount 6000.00 is at least 5000",
            "1500.00, Amount 1500.00 is at least 1000"})
    void describesCrossedThresholdinReason(String amount, String expectedReason) {
        // given
        Transaction transaction = transactionWithAmount(amount);

        // when
        Optional<RuleResult> result = rule.evaluate(transaction, new BinLookupResult.NotFound());

        // then
        assertTrue(result.isPresent());
        assertEquals(expectedReason, result.get().reason());
    }

    private static Transaction transactionWithAmount(String amount) {
        return new Transaction("411111", new BigDecimal(amount), COUNTRY_CODE_PL);
    }
}
