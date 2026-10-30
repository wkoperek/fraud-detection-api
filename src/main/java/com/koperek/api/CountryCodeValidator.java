package com.koperek.api;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Set;

public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        } else {
            return ISO_COUNTRIES.contains(value.toUpperCase());
        }
    }
}
