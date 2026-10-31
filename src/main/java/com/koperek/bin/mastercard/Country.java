package com.koperek.bin.mastercard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record Country(String alpha3) {
}
