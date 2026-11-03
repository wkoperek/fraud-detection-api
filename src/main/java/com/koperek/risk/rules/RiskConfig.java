package com.koperek.risk.rules;

import io.smallrye.config.ConfigMapping;

import java.math.BigDecimal;

@ConfigMapping(prefix = "risk")
interface RiskConfig {
    HighAmount highAmount();

    interface HighAmount {
        BigDecimal mediumThreshold();

        BigDecimal highThreshold();
    }
}
