package com.koperek.api;


import java.util.List;

public record ErrorResponse(String error, String message, List<Violation> violations, String requestId) {
    public record Violation(String field, String message) {
    }
}
