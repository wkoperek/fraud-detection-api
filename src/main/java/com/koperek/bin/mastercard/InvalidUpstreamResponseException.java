package com.koperek.bin.mastercard;

public class InvalidUpstreamResponseException extends RuntimeException {

    public InvalidUpstreamResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}