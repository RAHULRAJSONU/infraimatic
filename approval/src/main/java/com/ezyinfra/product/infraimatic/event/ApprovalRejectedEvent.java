package com.ezyinfra.product.infraimatic.event;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;

public record ApprovalRejectedEvent(ApprovalInstance instance)
        implements ApprovalEvent {}
