package com.ezyinfra.product.checkpost.identity.tenant.repository;

import com.ezyinfra.product.checkpost.identity.tenant.entity.TenantSigningKey;
import com.ezyinfra.product.checkpost.identity.tenant.model.KeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantSigningKeyRepository
        extends JpaRepository<TenantSigningKey, String> {

    Optional<TenantSigningKey> findByTenant_IdAndStatus(
            String tenantId,
            KeyStatus status
    );

    List<TenantSigningKey> findAllByTenant_Id(String tenantId);
}