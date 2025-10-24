package com.ezyinfra.product.infra.repository;

import com.ezyinfra.product.infra.entity.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for submissions (records). Provides tenant scoped accessors.
 */
public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    Optional<RecordEntity> findByIdAndTenantId(Long id, String tenantId);

    List<RecordEntity> findByTenantIdAndTypeAndTemplateVersion(String tenantId, String type, Integer templateVersion);
}