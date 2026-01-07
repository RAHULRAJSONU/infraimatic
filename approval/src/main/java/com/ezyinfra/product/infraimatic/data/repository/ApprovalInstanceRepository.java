package com.ezyinfra.product.infraimatic.data.repository;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ApprovalInstanceRepository extends JpaRepository<ApprovalInstance, UUID> {
    @Query("""
            select i from ApprovalInstance i
            where i.entityType = :type
            and i.entityId = :id
            and i.status = 'PENDING'
           """
    )
    Optional<ApprovalInstance> findActiveByEntity(
            @Param("type") String type,
            @Param("id") String id);

}