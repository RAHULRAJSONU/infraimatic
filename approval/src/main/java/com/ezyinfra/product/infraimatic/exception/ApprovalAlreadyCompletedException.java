package com.ezyinfra.product.infraimatic.exception;

public class ApprovalAlreadyCompletedException
        extends ApprovalException {

    public ApprovalAlreadyCompletedException() {
        super("APPROVAL_ALREADY_COMPLETED",
              "Approval is already completed");
    }
}
