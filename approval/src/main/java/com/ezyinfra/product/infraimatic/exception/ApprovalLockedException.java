package com.ezyinfra.product.infraimatic.exception;

public class ApprovalLockedException
        extends ApprovalException {

    public ApprovalLockedException() {
        super("ENTITY_LOCKED",
              "Entity is locked due to pending approval");
    }
}
