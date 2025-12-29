package com.ezyinfra.product.infra.repository;

import com.ezyinfra.product.infra.entity.RecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for submissions (records). Provides tenant scoped accessors.
 */
public interface RecordRepository extends JpaRepository<RecordEntity, UUID> {

    Optional<RecordEntity> findByIdAndTenantId(UUID id, String tenantId);

    List<RecordEntity> findByTypeAndTemplateVersion(String type, Integer templateVersion);

    Page<RecordEntity> findByTypeAndTemplateVersion(String type, Integer templateVersion, Pageable pageable);

    Page<RecordEntity> findByType(String type, Pageable pageable);
}