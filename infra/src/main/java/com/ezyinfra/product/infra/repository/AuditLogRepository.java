package com.ezyinfra.product.infra.repository;

import com.ezyinfra.product.infra.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for audit log entries.
 */
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}