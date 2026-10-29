package com.koperek.risk.rules;

import com.koperek.bin.BinInfo;
import com.koperek.bin.BinLookupResult;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrepaidCardRuleTest {
    private static final String COUNTRY_ALPHA_3_USA = "USA";
    private static final String COUNTRY_CODE_PL = "PL";
    private final PrepaidCardRule prepaidCardRule = new PrepaidCardRule();

    @Test
    void returnsOnePointWhenCardIsPrepaid() {
        // given
        BinLookupResult binLookupResult = new BinLookupResult.Found(new BinInfo(COUNTRY_ALPHA_3_USA, true));

        // when
        Optional<RuleResult> result = prepaidCardRule.evaluate(exampleTransaction(), binLookupResult);

        // then
        assertTrue(result.isPresent());
        assertEquals(PrepaidCardRule.NAME, result.get().rule());
        assertEquals(1, result.get().points());
        assertEquals(PrepaidCardRule.REASON_MESSAGE, result.get().reason());
    }

    @Test
    void returnsEmptyWhenCardIsNotPrepaid() {
        // given
        BinLookupResult binLookupResult = new BinLookupResult.Found(new BinInfo(COUNTRY_ALPHA_3_USA, false));

        // when
        Optional<RuleResult> result = prepaidCardRule.evaluate(exampleTransaction(), binLookupResult);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenBinNotFound() {
        // given
        BinLookupResult binLookupResult = new BinLookupResult.NotFound();

        // when
        Optional<RuleResult> result = prepaidCardRule.evaluate(exampleTransaction(), binLookupResult);

        //then
        assertTrue(result.isEmpty());
    }

    private Transaction exampleTransaction() {
        return new Transaction("411111", new BigDecimal("100.00"), COUNTRY_CODE_PL);
    }
}
