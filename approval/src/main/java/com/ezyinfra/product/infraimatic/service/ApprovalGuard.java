package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalInstanceRepository;
import org.springframework.stereotype.Service;

@Service
public class ApprovalGuard {

    private final ApprovalInstanceRepository instanceRepo;

    public ApprovalGuard(ApprovalInstanceRepository instanceRepo) {
        this.instanceRepo = instanceRepo;
    }

    public void ensureNotLocked(String entityType, String entityId) {
        instanceRepo.findActiveByEntity(entityType, entityId)
                .filter(ApprovalInstance::isLocked)
                .ifPresent(i -> {
                    throw new IllegalStateException(
                        "Entity is under approval and locked");
                });
    }
}
