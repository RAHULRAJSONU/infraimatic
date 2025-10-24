package com.ezyinfra.product.common.exception;

/**
 * Base class for custom exceptions in the Infraimatic platform.
 */
public abstract class InfraimaticException extends RuntimeException {
    protected InfraimaticException(String message) {
        super(message);
    }

    protected InfraimaticException(String message, Throwable cause) {
        super(message, cause);
    }
}