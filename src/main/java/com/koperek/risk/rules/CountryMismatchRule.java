package com.koperek.risk.rules;

import com.koperek.bin.BinInfo;
import com.koperek.bin.BinLookupResult;
import com.koperek.risk.RiskRule;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public final class CountryMismatchRule implements RiskRule {
    public static final String NAME = "COUNTRY_MISMATCH";
    private static final int POINTS = 2;

    @Override
    public Optional<RuleResult> evaluate(Transaction transaction, BinLookupResult binLookupResult) {
        return switch (binLookupResult) {
            case BinLookupResult.Found(BinInfo binInfo) -> check(transaction, binInfo);
            case BinLookupResult.NotFound() -> Optional.empty();
            case BinLookupResult.Unavailable() -> Optional.empty();
        };
    }

    private Optional<RuleResult> check(Transaction transaction, BinInfo binInfo) {
        String cardCountry = binInfo.countryAlpha3().toUpperCase();
        String transactionCountry = toAlpha3(transaction.countryCode());

        if (cardCountry.equals(transactionCountry)) {
            return Optional.empty();
        }

        return Optional.of(new RuleResult(NAME, POINTS,
                "Card issued in " + cardCountry + ", transaction in " + transactionCountry));
    }

    private static String toAlpha3(String alpha2Code) {
        Locale country = new Locale.Builder().setRegion(alpha2Code).build();
        return country.getISO3Country();
    }
}