package com.ezyinfra.product.storage.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "gdrive_resumable_uploads", indexes = {
    @Index(name = "idx_gdrive_upload_target", columnList = "targetPath")
})
public class ResumableUploadEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String uploadId; // UUID we generate for local tracking

    @Column(nullable = false, length = 2048)
    private String uploadUrl; // the Location header (resumable session URL)

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String targetPath;

    @Column
    private String contentType;

    @Column
    private long totalBytes = -1L; // -1 if unknown

    @Column
    private long bytesUploaded = 0L; // last uploaded byte index + 1 (i.e., next offset)

    @Column
    private String status; // IN_PROGRESS, COMPLETED, FAILED

    @Column
    private Instant createdAt = Instant.now();

    @Column
    private Instant updatedAt = Instant.now();
}