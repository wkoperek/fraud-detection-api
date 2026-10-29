package com.koperek.risk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RiskScorerTest {

    private final RiskScorer riskScorer = new RiskScorer();


    @Test
    void returnsVeryLowWhenNoRuleTriggered() {
        // given
        List<RuleResult> ruleResults = List.of();

        // when
        RiskLevel level = riskScorer.score(ruleResults);

        // then
        assertEquals(RiskLevel.VERY_LOW, level);
    }

    @ParameterizedTest
    @CsvSource({"1, LOW", "2, MEDIUM", "3, HIGH", "4, HIGH", "5, VERY_HIGH", "7, VERY_HIGH"})
    void mapsTotalPointsToLevel(int points, RiskLevel expectedLevel) {
        // given
        List<RuleResult> ruleResults = List.of(ruleResultWithPoints(points));

        // when
        RiskLevel level = riskScorer.score(ruleResults);

        // then
        assertEquals(expectedLevel, level);
    }

    @Test
    void sumsPointsAcrossRules() {
        // given
        List<RuleResult> ruleResults = List.of(ruleResultWithPoints(2), ruleResultWithPoints(1));

        // when
        RiskLevel level = riskScorer.score(ruleResults);

        // then
        assertEquals(RiskLevel.HIGH, level);
    }

    private static RuleResult ruleResultWithPoints(int points) {
        return new RuleResult("TEST_RULE", points, "test reason");
    }
}
