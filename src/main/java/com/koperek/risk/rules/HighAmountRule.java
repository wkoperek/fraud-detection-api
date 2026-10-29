package com.koperek.risk.rules;

import com.koperek.bin.BinLookupResult;
import com.koperek.risk.RiskRule;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;

import java.math.BigDecimal;
import java.util.Optional;

public final class HighAmountRule implements RiskRule {
    public static final String NAME = "HIGH_AMOUNT";
    private static final int MEDIUM_POINTS = 1;
    private static final int HIGH_POINTS = 2;

    private final BigDecimal mediumThreshold;
    private final BigDecimal highThreshold;

    public HighAmountRule(BigDecimal mediumThreshold, BigDecimal highThreshold) {
        this.mediumThreshold = mediumThreshold;
        this.highThreshold = highThreshold;
    }

    @Override
    public Optional<RuleResult> evaluate(Transaction transaction, BinLookupResult binLookupResult) {
        BigDecimal amount = transaction.amount();

        if (amount.compareTo(highThreshold) >= 0) {
            return Optional.of(new RuleResult(NAME, HIGH_POINTS, reason(amount, highThreshold)));
        } else if (amount.compareTo(mediumThreshold) >= 0) {
            return Optional.of(new RuleResult(NAME, MEDIUM_POINTS, reason(amount, mediumThreshold)));
        }

        return Optional.empty();
    }

    private static String reason(BigDecimal amount, BigDecimal threshold) {
        return "Amount " + amount.toPlainString() + " is at least " + threshold.toPlainString();
    }
}
