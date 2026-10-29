package com.koperek.risk;

import io.smallrye.config.ConfigMapping;

import java.math.BigDecimal;

@ConfigMapping(prefix = "risk")
public interface RiskConfig {
    HighAmount highAmount();

    interface HighAmount {
        BigDecimal mediumThreshold();

        BigDecimal highThreshold();
    }
}
