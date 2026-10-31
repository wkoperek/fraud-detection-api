package com.koperek.api;

import com.koperek.risk.RiskAssessment;
import com.koperek.risk.RiskLevel;
import com.koperek.risk.RuleResult;
import com.koperek.risk.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public final class TransactionMapper {
    private static final int MAX_SCORE = 5;
    private static final String NO_RISK_FACTORS_DETECTED_MESSAGE = "No risk factors detected";
    private static final String DELIMITER = "; ";

    public static Transaction toTransaction(TransactionRequest request) {
        return new Transaction(request.bin(), request.amount(), request.countryCode().toUpperCase());
    }

    public static TransactionResponse toResponse(RiskAssessment assessment, String requestId) {
        RiskLevel level = assessment.level();
        List<TriggeredRule> triggeredRules = assessment.triggeredRules().stream()
                .map(result -> new TriggeredRule(result.rule(), result.points(), result.reason()))
                .toList();

        return new TransactionResponse(level.score(), level.name(), assessmentText(level, assessment.triggeredRules()), triggeredRules, requestId);
    }

    private static String assessmentText(RiskLevel level, List<RuleResult> ruleResults) {
        if (ruleResults.isEmpty()) {
            return NO_RISK_FACTORS_DETECTED_MESSAGE;
        }

        String reasons = ruleResults.stream().map(RuleResult::reason).collect(Collectors.joining(DELIMITER));
        return constructResultMessage(level, reasons);
    }

    private static String constructResultMessage(RiskLevel level, String reasons) {
        return "Risk level " + level.name() + " (" + level.score() + "/" + MAX_SCORE + "). " + reasons;
    }
}
