package com.ezyinfra.product.infraimatic.validator;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalLevelRequest;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTemplateRequest;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ApprovalTemplateValidator {

    public void validate(ApprovalTemplateRequest req) {

        if (req.levels() == null || req.levels().isEmpty()) {
            throw new IllegalArgumentException("At least one approval level required");
        }

        // unique & sequential levelOrder
        Set<Integer> orders = new HashSet<>();
        for (ApprovalLevelRequest l : req.levels()) {

            if (!orders.add(l.levelOrder())) {
                throw new IllegalArgumentException(
                    "Duplicate levelOrder: " + l.levelOrder());
            }

            if (l.slaDuration().isNegative()
                || l.reminderBefore().isNegative()) {
                throw new IllegalArgumentException("SLA durations must be positive");
            }

            if (l.reminderBefore().compareTo(l.slaDuration()) >= 0) {
                throw new IllegalArgumentException(
                    "Reminder must be before SLA expiry");
            }
        }
    }
}
