package com.ezyinfra.product.infraimatic.data.repository;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalIntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalIntegrationRepository
        extends JpaRepository<ApprovalIntegrationConfig, Long> {

    Optional<ApprovalIntegrationConfig>
        findByEntityTypeAndEnabledTrue(String entityType);
}
