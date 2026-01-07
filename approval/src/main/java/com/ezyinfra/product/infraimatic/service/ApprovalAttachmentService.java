package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalAttachment;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTemplate;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalAttachmentRepository;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalTemplateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class ApprovalAttachmentService {

    private final ApprovalAttachmentRepository attachmentRepo;
    private final ApprovalTemplateRepository templateRepo;

    public ApprovalAttachmentService(
            ApprovalAttachmentRepository attachmentRepo,
            ApprovalTemplateRepository templateRepo) {
        this.attachmentRepo = attachmentRepo;
        this.templateRepo = templateRepo;
    }

    public void attach(String entityType, String entityId, UUID templateId) {
        ApprovalTemplate template = templateRepo.findById(templateId)
                .orElseThrow();

        ApprovalAttachment attachment = new ApprovalAttachment();
        attachment.setEntityType(entityType);
        attachment.setEntityId(entityId);
        attachment.setTemplate(template);

        attachmentRepo.save(attachment);
    }

    public void detach(String entityType, String entityId) {
        attachmentRepo.findByEntityTypeAndEntityId(entityType, entityId)
                .ifPresent(attachmentRepo::delete);
    }
}
