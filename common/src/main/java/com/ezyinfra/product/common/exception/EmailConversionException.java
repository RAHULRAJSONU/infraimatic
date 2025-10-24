package com.ezyinfra.product.common.exception;

public class EmailConversionException extends RuntimeException {

    public EmailConversionException() {
        super();
    }

    public EmailConversionException(final String message) {
        super(message);
    }

    public EmailConversionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EmailConversionException(final Throwable cause) {
        super(cause);
    }

}