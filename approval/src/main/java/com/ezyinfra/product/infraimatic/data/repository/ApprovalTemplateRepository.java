package com.ezyinfra.product.infraimatic.data.repository;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalTemplateRepository
        extends JpaRepository<ApprovalTemplate, UUID> {

    List<ApprovalTemplate> findByActiveTrue();
}
