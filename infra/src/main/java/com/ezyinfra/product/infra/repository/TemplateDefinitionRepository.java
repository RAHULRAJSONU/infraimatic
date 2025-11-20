package com.ezyinfra.product.infra.repository;

import com.ezyinfra.product.infra.entity.TemplateDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CRUD operations on template definitions. Includes helper
 * methods to fetch the latest version of a template for a tenant.
 */
public interface TemplateDefinitionRepository extends JpaRepository<TemplateDefinitionEntity, UUID> {

    Optional<TemplateDefinitionEntity> findByTenantIdAndTypeAndVersion(String tenantId, String type, Integer version);

    Optional<TemplateDefinitionEntity> findTopByTenantIdAndTypeOrderByVersionDesc(String tenantId, String type);

    List<TemplateDefinitionEntity> findByTenantIdAndTypeOrderByVersionDesc(String tenantId, String type);

    /**
     * Find the latest version of a template for a tenant and type.
     */
    @Query("SELECT t FROM TemplateDefinitionEntity t WHERE t.tenantId = :tenantId AND t.type = :type ORDER BY t.version DESC")
    List<TemplateDefinitionEntity> findLatest(@Param("tenantId") String tenantId, @Param("type") String type);
}