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

public class BinNotVerifiedRuleTest {
    private static final String COUNTRY_CODE_PL = "PL";
    private static final String COUNTRY_ALPHA_3_USA = "USA";
    private BinNotVerifiedRule binNotVerifiedRule = new BinNotVerifiedRule();

    @Test
    void returnsTwoPointsWhenBinNotFound() {
        // given
        final String binNumber = "999999";
        final BinLookupResult binLookupResult = new BinLookupResult.NotFound();
        Transaction transaction = transactionWithBin(binNumber);

        // when
        Optional<RuleResult> result = binNotVerifiedRule.evaluate(transaction, binLookupResult);

        // then
        assertTrue(result.isPresent());
        assertEquals(BinNotVerifiedRule.NAME, result.get().rule());
        assertEquals(2, result.get().points());
        assertEquals("BIN " + binNumber + " could not be verified", result.get().reason());
    }

    @Test
    void returnsEmptyWhenBinFound() {
        // given
        final String binNumber = "411111";
        final BinLookupResult binLookupResult = new BinLookupResult.Found(new BinInfo(COUNTRY_ALPHA_3_USA, false));
        Transaction transaction = transactionWithBin(binNumber);

        // when
        Optional<RuleResult> result = binNotVerifiedRule.evaluate(transaction, binLookupResult);

        // then
        assertTrue(result.isEmpty());
    }

    private Transaction transactionWithBin(String bin) {
        return new Transaction(bin, new BigDecimal("100.00"), COUNTRY_CODE_PL);
    }

}
