package com.koperek.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Comparator;
import java.util.List;

public class ApiExceptionMappers {

    private static final String VALIDATION_FAILED_MSG_CODE = "VALIDATION_FAILED";
    private static final String REQUEST_VALIDATION_FAILED_MSG = "Request validation failed";
    private static final char PROPERTY_PATH_SEPARATOR = '.';

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleViolation(ConstraintViolationException exception) {
        List<ErrorResponse.Violation> violations = exception.getConstraintViolations().stream()
                .map(ApiExceptionMappers::toViolation)
                .sorted(Comparator.comparing(ErrorResponse.Violation::field)
                        .thenComparing(ErrorResponse.Violation::message))
                .toList();
        ErrorResponse errorResponse = new ErrorResponse(VALIDATION_FAILED_MSG_CODE, REQUEST_VALIDATION_FAILED_MSG, violations, null);
        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse);
    }

    private static ErrorResponse.Violation toViolation(ConstraintViolation<?> constraintViolation) {
        String propertyPath = constraintViolation.getPropertyPath().toString();
        String fieldPath = propertyPath.substring(propertyPath.lastIndexOf(PROPERTY_PATH_SEPARATOR) + 1);
        String message = constraintViolation.getMessage();
        return new ErrorResponse.Violation(fieldPath, message);
    }
}
