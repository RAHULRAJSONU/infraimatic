package com.ezyinfra.product.infraimatic.event;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;

public record ApprovalApprovedEvent(ApprovalInstance instance)
        implements ApprovalEvent {}