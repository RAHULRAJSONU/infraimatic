package com.ezyinfra.product.infra.repository;

import com.ezyinfra.product.infra.entity.AttributeDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for attribute definitions. Attributes may be global (tenantId null)
 * or tenant specific.
 */
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinitionEntity, String> {

    List<AttributeDefinitionEntity> findByTenantIdOrTenantIdIsNull(String tenantId);
}