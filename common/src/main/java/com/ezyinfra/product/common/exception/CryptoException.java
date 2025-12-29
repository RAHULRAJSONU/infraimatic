package com.ezyinfra.product.common.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CryptoException extends RuntimeException {
    private final HttpStatus statusCode;

    public CryptoException(String message) {
        super(message);
        this.statusCode = HttpStatus.BAD_REQUEST;
    }

    public CryptoException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CryptoException(String message, Throwable throwable) {
        super(message, throwable);
        this.statusCode = HttpStatus.BAD_REQUEST;
    }
}