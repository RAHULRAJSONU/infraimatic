package com.ezyinfra.product.infraimatic.exception;

public class ApprovalUnauthorizedException
        extends ApprovalException {

    public ApprovalUnauthorizedException() {
        super("NOT_AUTHORIZED",
              "You are not authorized to act on this approval");
    }
}
