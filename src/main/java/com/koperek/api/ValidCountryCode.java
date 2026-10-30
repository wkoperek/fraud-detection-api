package com.koperek.api;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCode {

    String message() default "Must be a valid ISO 3166-1 alpha-2 country code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
