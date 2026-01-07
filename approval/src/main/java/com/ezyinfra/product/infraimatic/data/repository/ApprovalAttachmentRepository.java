package com.ezyinfra.product.infraimatic.data.repository;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalAttachmentRepository
        extends JpaRepository<ApprovalAttachment, Long> {

    Optional<ApprovalAttachment> findByEntityTypeAndEntityId(String entityType, String entityId);
}
