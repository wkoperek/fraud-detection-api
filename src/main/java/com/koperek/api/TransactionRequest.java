package com.koperek.api;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank @Pattern(regexp = "^\\d{6,8}$") String bin,
        @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") @ValidCountryCode String countryCode) {
}