package com.ezyinfra.product.infraimatic.exception;

public abstract class ApprovalException extends RuntimeException {

    private final String errorCode;

    protected ApprovalException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
