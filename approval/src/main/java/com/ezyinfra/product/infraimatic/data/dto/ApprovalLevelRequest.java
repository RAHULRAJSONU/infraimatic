package com.ezyinfra.product.infraimatic.data.dto;

import java.time.Duration;

public record ApprovalLevelRequest(
        int levelOrder,
        String approverType,        // USER | ROLE
        String approver,
        Duration slaDuration,       // PT4H
        Duration reminderBefore,    // PT30M
        String escalationRole
) {}
