package com.koperek.risk;

import java.math.BigDecimal;

public record Transaction(String bin, BigDecimal amount, String countryCode) {

}