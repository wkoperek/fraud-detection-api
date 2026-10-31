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
    public static final String SPACE = " ";
    private static final String DELIMITER = "; ";
    private static final String DEGRADED_MESSAGE = "BIN data was unavailable; assessment is based on transaction data only.";

    public static Transaction toTransaction(TransactionRequest request) {
        return new Transaction(request.bin(), request.amount(), request.countryCode().toUpperCase());
    }

    public static TransactionResponse toResponse(RiskAssessment assessment, String requestId) {
        RiskLevel level = assessment.level();
        List<TriggeredRule> triggeredRules = assessment.triggeredRules().stream()
                .map(result -> new TriggeredRule(result.rule(), result.points(), result.reason()))
                .toList();

        return new TransactionResponse(level.score(), level.name(), assessmentText(level, assessment.triggeredRules(), assessment.degraded()), triggeredRules, requestId, assessment.degraded());
    }

    private static String assessmentText(RiskLevel level, List<RuleResult> ruleResults, boolean degraded) {
        String text = ruleResults.isEmpty() ? NO_RISK_FACTORS_DETECTED_MESSAGE : constructResultMessage(level, joinReasons(ruleResults));
        return degraded ? text + SPACE + DEGRADED_MESSAGE : text;
    }

    private static String joinReasons(List<RuleResult> ruleResults) {
        return ruleResults.stream().map(RuleResult::reason).collect(Collectors.joining(DELIMITER));
    }

    private static String constructResultMessage(RiskLevel level, String reasons) {
        return "Risk level " + level.name() + " (" + level.score() + "/" + MAX_SCORE + "). " + reasons;
    }
}
