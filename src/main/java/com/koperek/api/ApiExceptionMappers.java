package com.koperek.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.logging.Log;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Comparator;
import java.util.List;

public class ApiExceptionMappers {

    private static final String VALIDATION_FAILED_MSG_CODE = "VALIDATION_FAILED";
    private static final String REQUEST_VALIDATION_FAILED_MSG = "Request validation failed";
    private static final String MALFORMED_REQUEST_MSG_CODE = "MALFORMED_REQUEST";
    private static final String MALFORMED_REQUEST_MSG = "Request body is malformed";
    private static final char PROPERTY_PATH_SEPARATOR = '.';
    private static final char FLOOR_CHAR = '_';
    private static final char SEPARATOR_CHAR = ' ';
    public static final String ITERNAL_ERROR_MSG_CODE = "ITERNAL_ERROR";
    public static final String INTERNAL_SERVER_ERROR_MSG = "Internal server error";

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

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleConstraintViolation(JsonProcessingException exception) {
        return malformedRequest();
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleWebApplication(WebApplicationException exception) {
        if (exception.getCause() instanceof JsonProcessingException) {
            return malformedRequest();
        } else {
            Response response = exception.getResponse();
            String reason = response.getStatusInfo().getReasonPhrase();
            String code = reason.toUpperCase().replace(SEPARATOR_CHAR, FLOOR_CHAR);
            ErrorResponse errorResponse = new ErrorResponse(code, reason, null, null);
            return RestResponse.status(response.getStatusInfo(), errorResponse);
        }
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleUnexpected(Exception exception) {
        Log.error("Unexpected error", exception);
        ErrorResponse errorResponse = new ErrorResponse(ITERNAL_ERROR_MSG_CODE, INTERNAL_SERVER_ERROR_MSG, null, null);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, errorResponse);
    }

    private static RestResponse<ErrorResponse> malformedRequest() {
        ErrorResponse errorResponse = new ErrorResponse(MALFORMED_REQUEST_MSG_CODE, MALFORMED_REQUEST_MSG, null, null);
        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse);
    }

    private static ErrorResponse.Violation toViolation(ConstraintViolation<?> constraintViolation) {
        String propertyPath = constraintViolation.getPropertyPath().toString();
        String fieldPath = propertyPath.substring(propertyPath.lastIndexOf(PROPERTY_PATH_SEPARATOR) + 1);
        String message = constraintViolation.getMessage();
        return new ErrorResponse.Violation(fieldPath, message);
    }
}
