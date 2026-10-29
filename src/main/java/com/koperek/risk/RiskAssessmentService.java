package com.koperek.risk;

import com.koperek.bin.BinLookupResult;
import com.koperek.bin.BinLookupService;
import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RiskAssessmentService {
    private final List<RiskRule> rules;
    private final BinLookupService binLookupService;
    private final RiskScorer scorer;

    public RiskAssessmentService(@All List<RiskRule> rules, BinLookupService binLookupService, RiskScorer scorer) {
        this.rules = rules;
        this.binLookupService = binLookupService;
        this.scorer = scorer;
    }

    public RiskAssessment assess(Transaction transaction) {
        BinLookupResult binLookupResult = binLookupService.lookup(transaction.bin());

        List<RuleResult> triggeredRules = rules.stream()
                .map(rule -> rule.evaluate(transaction, binLookupResult))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(RuleResult::rule))
                .toList();

        RiskLevel level = scorer.score(triggeredRules);
        return new RiskAssessment(level, triggeredRules);
    }
}
