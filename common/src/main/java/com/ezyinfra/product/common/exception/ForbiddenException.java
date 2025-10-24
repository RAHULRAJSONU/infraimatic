package com.ezyinfra.product.common.exception;

/**
 * Thrown when a caller does not have permission to perform an operation.
 */
public class ForbiddenException extends InfraimaticException {
    public ForbiddenException(String message) {
        super(message);
    }
}