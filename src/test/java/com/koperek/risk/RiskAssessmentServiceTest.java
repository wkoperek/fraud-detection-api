package com.koperek.risk;

import com.koperek.bin.BinInfo;
import com.koperek.bin.BinLookupResult;
import com.koperek.bin.BinLookupService;
import com.koperek.risk.rules.BinNotVerifiedRule;
import com.koperek.risk.rules.CountryMismatchRule;
import com.koperek.risk.rules.HighAmountRule;
import com.koperek.risk.rules.PrepaidCardRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskAssessmentServiceTest {
    private static final String COUNTRY_CODE_PL = "PL";
    private static final String COUNTRY_ALPHA_3_USA = "USA";
    private static final String LOW_AMOUNT = "50";

    @Test
    void returnsVeryLowWhenNoRuleTriggered() {
        // given
        RiskAssessmentService service = serviceWithLookup(new BinLookupResult.Found(new BinInfo("DEU", false)));
        Transaction transaction = new Transaction("222100", new BigDecimal(LOW_AMOUNT), "DE");

        // when
        RiskAssessment assessment = service.assess(transaction);

        // then
        assertEquals(RiskLevel.VERY_LOW, assessment.level());
        assertTrue(assessment.triggeredRules().isEmpty());
    }

    @Test
    void sumsCountryMismatchAndHighAmount() {
        // given
        RiskAssessmentService service = serviceWithLookup(new BinLookupResult.Found(new BinInfo(COUNTRY_ALPHA_3_USA, false)));
        Transaction transaction = new Transaction("411111", new BigDecimal("1500"), COUNTRY_CODE_PL);

        // when
        RiskAssessment assessment = service.assess(transaction);

        // then
        assertEquals(RiskLevel.HIGH, assessment.level());
        assertEquals(List.of(CountryMismatchRule.NAME, HighAmountRule.NAME), ruleNames(assessment));
    }

    @Test
    void returnsTriggeredRulesSortedByName() {
        // given
        RiskAssessmentService service = serviceWithLookup(new BinLookupResult.Found(new BinInfo(COUNTRY_ALPHA_3_USA, true)));
        Transaction transaction = new Transaction("511111", new BigDecimal("6000"), COUNTRY_CODE_PL);

        // when
        RiskAssessment assessment = service.assess(transaction);

        // then
        assertEquals(RiskLevel.VERY_HIGH, assessment.level());
        assertEquals(List.of(CountryMismatchRule.NAME, HighAmountRule.NAME, PrepaidCardRule.NAME), ruleNames(assessment));
    }

    @Test
    void marksBinAsNotVerifiedWhenLookupFindsNothing() {
        // given
        RiskAssessmentService service = serviceWithLookup(new BinLookupResult.NotFound());
        Transaction transaction = new Transaction("999999", new BigDecimal(LOW_AMOUNT), COUNTRY_CODE_PL);

        // when
        RiskAssessment assessment = service.assess(transaction);

        // then
        assertEquals(RiskLevel.MEDIUM, assessment.level());
        assertEquals(List.of(BinNotVerifiedRule.NAME), ruleNames(assessment));
    }

    private static RiskAssessmentService serviceWithLookup(BinLookupResult lookupResult) {
        BinLookupService binLookupService = bin -> lookupResult;
        List<RiskRule> rules = List.of(
                new PrepaidCardRule(),
                new HighAmountRule(new BigDecimal("1000"), new BigDecimal("5000")),
                new CountryMismatchRule(),
                new BinNotVerifiedRule());
        return new RiskAssessmentService(rules, binLookupService, new RiskScorer());
    }

    private static List<String> ruleNames(RiskAssessment assessment) {
        return assessment.triggeredRules().stream().map(RuleResult::rule).toList();
    }
}