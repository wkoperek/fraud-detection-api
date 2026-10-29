package com.koperek.risk;

import com.koperek.bin.BinLookupResult;

import java.util.Optional;

public interface RiskRule {
    Optional<RuleResult> evaluate(Transaction transaction, BinLookupResult binLookupResult);
}
