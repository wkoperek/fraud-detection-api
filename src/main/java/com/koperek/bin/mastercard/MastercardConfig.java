package com.koperek.bin.mastercard;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "mastercard")
public interface MastercardConfig {
    String consumerKey();
    String keystorePath();
    String keystorePassword();
    String keyAlias();
}
