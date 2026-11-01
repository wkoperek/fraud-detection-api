package com.koperek.risk.rules;

import com.koperek.risk.RiskConfig;
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
