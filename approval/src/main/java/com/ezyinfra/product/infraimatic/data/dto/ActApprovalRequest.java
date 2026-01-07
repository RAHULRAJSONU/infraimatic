package com.ezyinfra.product.infraimatic.data.dto;

import java.util.Set;

public record ActApprovalRequest(
        boolean approve,
        String comment,
        Set<String> roles) {}
