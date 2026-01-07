package com.ezyinfra.product.infraimatic.exception;

public class ApprovalNotFoundException
        extends ApprovalException {

    public ApprovalNotFoundException(String id) {
        super("APPROVAL_NOT_FOUND",
              "Approval request not found: " + id);
    }
}
