package com.koperek.risk.rules;

import com.koperek.bin.BinLookupResult;
import com.koperek.risk.RiskRule;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;

import java.util.Optional;

public final class BinNotVerifiedRule implements RiskRule {
    public static final String NAME = "BIN_NOT_VERIFIED";
    private static final int POINTS = 2;

    @Override
    public Optional<RuleResult> evaluate(Transaction transaction, BinLookupResult binLookupResult) {

        return switch (binLookupResult){
            case BinLookupResult.NotFound() -> Optional.of(new RuleResult(NAME, POINTS, constructReasonMessage(transaction)));
            case BinLookupResult.Found ignored -> Optional.empty();
        };
    }

    private static String constructReasonMessage(Transaction transaction) {
        return "BIN " + transaction.bin() + " could not be verified";
    }
}
