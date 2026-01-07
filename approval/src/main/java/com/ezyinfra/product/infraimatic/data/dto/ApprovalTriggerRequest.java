package com.ezyinfra.product.infraimatic.data.dto;

import java.util.List;

public record ApprovalTriggerRequest(
    String entityType,
    String entityId,
    List<RuntimeApproverRequest> runtimeApprovers
) {}
