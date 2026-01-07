package com.ezyinfra.product.infraimatic.exception;

public class ApprovalInvalidStateException
        extends ApprovalException {

    public ApprovalInvalidStateException(String message) {
        super("INVALID_STATE", message);
    }
}
