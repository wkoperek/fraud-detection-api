package com.koperek.risk.rules;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class RiskRuleProducers {

    @Produces
    @ApplicationScoped
    HighAmountRule highAmountRule(RiskConfig config) {
        return new HighAmountRule(config.highAmount().mediumThreshold(), config.highAmount().highThreshold());
    }
}
