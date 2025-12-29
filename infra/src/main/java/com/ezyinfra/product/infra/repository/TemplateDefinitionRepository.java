package com.ezyinfra.product.infra.repository;

import com.ezyinfra.product.infra.entity.TemplateDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CRUD operations on template definitions. Includes helper
 * methods to fetch the latest version of a template for a tenant.
 */
public interface TemplateDefinitionRepository extends JpaRepository<TemplateDefinitionEntity, UUID> {

    Optional<TemplateDefinitionEntity> findByTypeAndVersion(String type, Integer version);

    Optional<TemplateDefinitionEntity> findTopByTypeOrderByVersionDesc(String type);

    List<TemplateDefinitionEntity> findByTypeOrderByVersionDesc(String type);
}