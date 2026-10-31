package com.koperek.risk.rules;

import com.koperek.bin.BinInfo;
import com.koperek.bin.BinLookupResult;
import com.koperek.risk.RiskRule;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public final class PrepaidCardRule implements RiskRule {
    public static final String NAME = "PREPAID_CARD";
    private static final int POINTS = 1;
    public static final String REASON_MESSAGE = "Card is prepaid!";


    @Override
    public Optional<RuleResult> evaluate(Transaction transaction, BinLookupResult binLookupResult) {
        return switch (binLookupResult) {
            case BinLookupResult.Found(BinInfo binInfo) -> checkIfPrepaid(binInfo);
            case BinLookupResult.NotFound() -> Optional.empty();
            case BinLookupResult.Unavailable() -> Optional.empty();
        };
    }

    private Optional<RuleResult> checkIfPrepaid(BinInfo binInfo) {
        if (binInfo.isPrepaid()) {
            return Optional.of(new RuleResult(NAME, POINTS, REASON_MESSAGE));
        }

        return Optional.empty();
    }
}