package com.ezyinfra.product.common.exception;

/**
 * Thrown when an entity cannot be found in the system.
 */
public class NotFoundException extends InfraimaticException {
    public NotFoundException(String message) {
        super(message);
    }
}