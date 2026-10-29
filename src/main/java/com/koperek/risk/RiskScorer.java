package com.koperek.risk;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RiskScorer {

    public RiskLevel score(List<RuleResult> ruleResults) {
        int totalPoints = ruleResults.stream().mapToInt(RuleResult::points).sum();
        return levelFor(totalPoints);
    }

    private RiskLevel levelFor(int points) {
        return switch (points) {
            case 0 -> RiskLevel.VERY_LOW;
            case 1 -> RiskLevel.LOW;
            case 2 -> RiskLevel.MEDIUM;
            case 3, 4 -> RiskLevel.HIGH;
            default -> RiskLevel.VERY_HIGH;
        };
    }
}
