package com.ezyinfra.product.storage.repository;

import com.ezyinfra.product.storage.model.ResumableUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumableUploadRepository extends JpaRepository<ResumableUploadEntity, UUID> {
    Optional<ResumableUploadEntity> findByTargetPathAndStatus(String targetPath, String status);
}
