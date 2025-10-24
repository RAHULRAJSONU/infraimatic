package com.ezyinfra.product.common.exception;

import java.io.Serial;

public class DuplicateRecordException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 9064651223148762476L;

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
