package com.koperek.risk;

import java.util.List;

public record RiskAssessment(RiskLevel level, List<RuleResult> triggeredRules, boolean degraded) {

}