package com.ezyinfra.product.common.exception;

import java.util.Map;

/**
 * Thrown when a request fails business or schema validation. Carries details
 * describing the specific validation violations.
 */
public class ValidationException extends InfraimaticException {
    private final Map<String, Object> details;

    public ValidationException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}