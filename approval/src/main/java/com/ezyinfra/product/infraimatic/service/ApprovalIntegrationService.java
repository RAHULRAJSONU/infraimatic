package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalIntegrationRequest;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalIntegrationConfig;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalIntegrationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ApprovalIntegrationService {

    private final ApprovalIntegrationRepository repo;

    public ApprovalIntegrationService(
            ApprovalIntegrationRepository repo) {
        this.repo = repo;
    }

    public ApprovalIntegrationConfig save(
            ApprovalIntegrationRequest r) {

        ApprovalIntegrationConfig c =
                repo.findByEntityTypeAndEnabledTrue(r.entityType())
                    .orElse(new ApprovalIntegrationConfig());

        c.setEntityType(r.entityType());
        c.setEnabled(r.enabled());
        c.setApprovalTemplateId(r.approvalTemplateId());
        c.setAssignmentStrategy(r.assignmentStrategy());
        c.setRequireAllApprovals(r.requireAllApprovals());
        c.setLevelOrder(r.levelOrder());

        return repo.save(c);
    }
}
