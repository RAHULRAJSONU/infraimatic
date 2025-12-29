package com.ezyinfra.product.common.exception;

public class WebhookIgnoredException extends RuntimeException {

    public WebhookIgnoredException(String message) {
        super(message);
    }

    public WebhookIgnoredException(String message, Throwable cause) {
        super(message, cause);
    }
}