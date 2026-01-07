package com.ezyinfra.product.infraimatic.data.dto;

public enum ApprovalTaskStatus {
    BLOCKED,   // waiting for previous group
    PENDING,   // active
    APPROVED,
    REJECTED
}
