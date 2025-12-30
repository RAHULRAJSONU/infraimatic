package com.ezyinfra.product.checkpost.identity.tenant.repository;

import com.ezyinfra.product.checkpost.identity.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TenantRepository
        extends JpaRepository<Tenant, String>,
        JpaSpecificationExecutor<Tenant> {

    Optional<Tenant> findByCode(String code);
}