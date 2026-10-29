package com.koperek.risk;

import org.apache.commons.lang3.StringUtils;

public record RuleResult(String rule, int points, String reason) {
    public RuleResult {

        if (StringUtils.isEmpty(rule) || StringUtils.isEmpty(reason)) {
            throw new IllegalArgumentException("Rule or reason is empty");
        }

        if (points <= 0) {
            throw new IllegalArgumentException("Points must be greater than 0");
        }
    }
}