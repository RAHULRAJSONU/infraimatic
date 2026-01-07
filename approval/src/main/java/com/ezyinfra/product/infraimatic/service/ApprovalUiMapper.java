package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalAction;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTaskStatus;
import com.ezyinfra.product.infraimatic.data.dto.PendingApprovalUiDto;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalLevel;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalUiMapper {

    public PendingApprovalUiDto toPendingUi(ApprovalTask task) {

        // UI must NEVER see blocked tasks
        if (task.getStatus() != ApprovalTaskStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING tasks can be mapped to UI");
        }

        ApprovalInstance instance = task.getInstance();

        ApprovalLevel levelPolicy = instance.getTemplate()
                .getLevels()
                .stream()
                .filter(l -> l.getLevelOrder() == task.getLevelOrder())
                .findFirst()
                .orElseThrow();

        return new PendingApprovalUiDto(
                instance.getId(),
                instance.getEntityType(),
                instance.getEntityId(),
                task.getLevelOrder(),
                task.getApprovalGroup(),
                levelPolicy.getAssignmentStrategy().name(),
                levelPolicy.isRequireAllApprovals(),
                task.getDueAt(),
                allowedActions(task)
        );
    }

    private List<String> allowedActions(ApprovalTask task) {

        // In future you can restrict actions dynamically here
        if (task.getStatus() == ApprovalTaskStatus.PENDING) {
            return List.of(
                    ApprovalAction.APPROVE.name(),
                    ApprovalAction.REJECT.name()
            );
        }

        return List.of();
    }
}

