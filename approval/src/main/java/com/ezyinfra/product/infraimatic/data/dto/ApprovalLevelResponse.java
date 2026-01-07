package com.ezyinfra.product.infraimatic.data.dto;

import java.time.Duration;

public record ApprovalLevelResponse(
    int levelOrder,
    String approverType,
    String approver,
    Duration slaDuration,
    Duration reminderBefore,
    String escalationRole
) {}
