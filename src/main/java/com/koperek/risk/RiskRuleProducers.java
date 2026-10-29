package com.koperek.risk;

import com.koperek.risk.rules.HighAmountRule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class RiskRuleProducers {

    @Produces
    @ApplicationScoped
    public HighAmountRule highAmountRule(RiskConfig config) {
        return new HighAmountRule(config.highAmount().mediumThreshold(), config.highAmount().highThreshold());
    }
}
