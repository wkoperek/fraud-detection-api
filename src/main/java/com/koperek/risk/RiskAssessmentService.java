package com.koperek.risk;

import com.koperek.bin.BinLookupResult;
import com.koperek.bin.BinLookupService;
import io.quarkus.arc.All;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class RiskAssessmentService {
    private static final String COMMA_DELIMITER = ",";
    private static final String FOUND = "found";
    private static final String NOT_FOUND = "not_found";

    private final List<RiskRule> rules;
    private final BinLookupService binLookupService;
    private final RiskScorer scorer;

    public RiskAssessmentService(@All List<RiskRule> rules, BinLookupService binLookupService, RiskScorer scorer) {
        this.rules = rules;
        this.binLookupService = binLookupService;
        this.scorer = scorer;
    }

    public RiskAssessment assess(Transaction transaction) {
        Log.debugf("assess bin=%s countryCode=%s amount=%s", transaction.bin(), transaction.countryCode(), transaction.amount());

        BinLookupResult binLookupResult = binLookupService.lookup(transaction.bin());
        List<RuleResult> triggeredRules = evaluateRules(transaction, binLookupResult);
        RiskLevel level = scorer.score(triggeredRules);

        logAssessment(binLookupResult, level, triggeredRules);
        return new RiskAssessment(level, triggeredRules);
    }

    private List<RuleResult> evaluateRules(Transaction transaction, BinLookupResult binLookupResult) {
        return rules.stream()
                .map(rule -> rule.evaluate(transaction, binLookupResult))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(RuleResult::rule))
                .toList();
    }

    private static void logAssessment(BinLookupResult binLookupResult, RiskLevel level, List<RuleResult> triggeredRules) {
        int points = triggeredRules.stream().mapToInt(RuleResult::points).sum();
        String ruleNames = triggeredRules.stream().map(RuleResult::rule).collect(Collectors.joining(COMMA_DELIMITER));
        Log.infof("assessment lookup=%s level=%s points=%d rules=%s",
                lookupOutcome(binLookupResult), level, points, ruleNames);
    }

    private static String lookupOutcome(BinLookupResult result) {
        return switch (result) {
            case BinLookupResult.Found found -> FOUND;
            case BinLookupResult.NotFound notFound -> NOT_FOUND;
        };
    }
}
