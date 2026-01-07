package com.ezyinfra.product.infraimatic.data.dto;

public enum AssignmentStrategy {
    FIXED,          // existing (USER / ROLE)
    RUNTIME_SINGLE, // one user decided at runtime
    RUNTIME_MULTI   // multiple users decided at runtime
}
