package com.ezyinfra.product.infraimatic.exception;

public class ApprovalConcurrencyException
        extends ApprovalException {

    public ApprovalConcurrencyException() {
        super("CONCURRENT_MODIFICATION",
              "Approval already processed by another user");
    }
}
