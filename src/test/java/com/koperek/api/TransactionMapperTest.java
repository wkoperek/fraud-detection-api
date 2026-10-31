package com.koperek.api;

import com.koperek.risk.RiskAssessment;
import com.koperek.risk.RiskLevel;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionMapperTest {
    private static final String BIN = "411111";
    private static final String COUNTRY_CODE_PL = "PL";
    private static final String COUNTRY_MISMATCH = "COUNTRY_MISMATCH";
    private static final String HIGH_AMOUNT = "HIGH_AMOUNT";
    private static final String COUNTRY_MISMATCH_REASON = "Card issued in USA, transaction in POL";
    private static final String HIGH_AMOUNT_REASON = "Amount 6000.00 is at least 5000";
    private static final String DELIMITER = "; ";
    private static final String REQUEST_ID = "test-id";

    @Test
    void mapsRequestToTransactionWithUpperCaseCountryCode() {
        // given
        TransactionRequest request = new TransactionRequest(BIN, new BigDecimal("6000.00"), COUNTRY_CODE_PL.toUpperCase());

        // when
        Transaction transaction = TransactionMapper.toTransaction(request);

        // then
        assertEquals(new Transaction(BIN, new BigDecimal("6000.00"), COUNTRY_CODE_PL), transaction);
    }

    @Test
    void describesAbsenceOfRiskFactorsWhenNoRuleTriggered() {
        // given
        RiskAssessment assessment = new RiskAssessment(RiskLevel.VERY_LOW, List.of());

        // when
        TransactionResponse response = TransactionMapper.toResponse(assessment, REQUEST_ID);

        // then
        assertEquals(1, response.riskScore());
        assertEquals(RiskLevel.VERY_LOW.name(), response.riskLevel());
        assertEquals("No risk factors detected", response.assessment());
        assertTrue(response.triggeredRules().isEmpty());
    }

    @Test
    void joinsReasonsOfTriggeredRulesIntoAssessment() {
        // given
        RiskAssessment assessment = new RiskAssessment(RiskLevel.HIGH, List.of(
                new RuleResult(COUNTRY_MISMATCH, 2, COUNTRY_MISMATCH_REASON),
                new RuleResult(HIGH_AMOUNT, 2, HIGH_AMOUNT_REASON)));

        // when
        TransactionResponse response = TransactionMapper.toResponse(assessment, REQUEST_ID);

        // then
        assertEquals(4, response.riskScore());
        assertEquals(RiskLevel.HIGH.name(), response.riskLevel());
        assertEquals("Risk level HIGH (4/5). " + COUNTRY_MISMATCH_REASON + DELIMITER + HIGH_AMOUNT_REASON,
                response.assessment());
        assertEquals(List.of(
                new TriggeredRule(COUNTRY_MISMATCH, 2, COUNTRY_MISMATCH_REASON),
                new TriggeredRule(HIGH_AMOUNT, 2, HIGH_AMOUNT_REASON)), response.triggeredRules());
    }
}