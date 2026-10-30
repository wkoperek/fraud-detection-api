package com.koperek.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank @Pattern(regexp = "^\\d{6,8}$") String bin,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") @ValidCountryCode String countryCode) {
}