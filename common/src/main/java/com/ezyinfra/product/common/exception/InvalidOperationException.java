package com.ezyinfra.product.common.exception;

import java.io.Serial;

public class InvalidOperationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5060198602766765786L;

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
