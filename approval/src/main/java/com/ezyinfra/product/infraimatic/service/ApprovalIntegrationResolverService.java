package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalContext;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTriggerRequest;
import com.ezyinfra.product.infraimatic.data.dto.AssignmentStrategy;
import com.ezyinfra.product.infraimatic.data.dto.RuntimeApproverRequest;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalIntegrationConfig;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalIntegrationRepository;
import com.ezyinfra.product.infraimatic.exception.ApprovalInvalidStateException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class ApprovalIntegrationResolverService {

    private final ApprovalIntegrationRepository integrationRepo;
    private final ApprovalAttachmentService attachmentService;
    private final ApprovalOrchestratorService orchestrator;

    public ApprovalIntegrationResolverService(
            ApprovalIntegrationRepository integrationRepo,
            ApprovalAttachmentService attachmentService,
            ApprovalOrchestratorService orchestrator) {
        this.integrationRepo = integrationRepo;
        this.attachmentService = attachmentService;
        this.orchestrator = orchestrator;
    }

    @Transactional
    public void resolveAndTrigger(
            String entityType,
            String entityId,
            ApprovalContext context
    ) {

        ApprovalIntegrationConfig config =
                integrationRepo.findByEntityTypeAndEnabledTrue(entityType)
                        .orElse(null);

        if (config == null || !config.isEnabled()) {
            log.debug("No approval integration for {}", entityType);
            return;
        }

        if (config.isRequireApproverInput() &&
                (context == null ||
                        context.approvers() == null ||
                        context.approvers().isEmpty())) {

            throw new ApprovalInvalidStateException(
                    "Approver input is required");
        }

        validateStrategy(config, context);

        // Attach template
        attachmentService.attach(
                entityType,
                entityId,
                config.getApprovalTemplateId()
        );

        // Trigger approval
        ApprovalTriggerRequest trigger =
                new ApprovalTriggerRequest(
                        entityType,
                        entityId,
                        List.of(
                                new RuntimeApproverRequest(
                                        config.getLevelOrder(),
                                        context.approvers()
                                )
                        )
                );

        orchestrator.triggerWithRuntimeApprovers(trigger);

        log.info(
                "Approval triggered for {}:{} with approvers {}",
                entityType, entityId, context.approvers()
        );
    }

    private void validateStrategy(
            ApprovalIntegrationConfig config,
            ApprovalContext context) {

        if (config.getAssignmentStrategy() == AssignmentStrategy.FIXED) {
            throw new ApprovalInvalidStateException(
                    "FIXED strategy cannot accept runtime approvers");
        }

        if (config.getAssignmentStrategy() == AssignmentStrategy.RUNTIME_SINGLE &&
                context.approvers().size() != 1) {

            throw new ApprovalInvalidStateException(
                    "Exactly one approver required");
        }
    }
}
