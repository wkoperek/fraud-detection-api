package com.koperek.bin.mastercard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record BinRange(String fundingSource, Country country) {

}
