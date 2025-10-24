package com.ezyinfra.product.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {
    private final HttpStatus statusCode;

    public AuthException(String message) {
        super(message);
        this.statusCode = HttpStatus.BAD_REQUEST;
    }

    public AuthException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public AuthException(String message, Throwable throwable) {
        super(message, throwable);
        this.statusCode = HttpStatus.BAD_REQUEST;
    }
}