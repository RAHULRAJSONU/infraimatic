package com.ezyinfra.product.common.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Standardised error envelope returned by the API in case of an exception.
 * Contains a human readable message, a machine readable error code, and
 * additional details. A timestamp is included to aid troubleshooting.
 */
public record ErrorResponse(
        String error,
        String message,
        Map<String, Object> details,
        Instant timestamp
) {
    public static ErrorResponse of(String error, String message, Map<String, Object> details) {
        return new ErrorResponse(error, message, details, Instant.now());
    }
}