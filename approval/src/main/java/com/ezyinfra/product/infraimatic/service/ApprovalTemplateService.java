package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalLevelRequest;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalLevelResponse;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTemplateRequest;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTemplateResponse;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalLevel;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTemplate;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalTemplateRepository;
import com.ezyinfra.product.infraimatic.validator.ApprovalTemplateValidator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApprovalTemplateService {

    private final ApprovalTemplateRepository templateRepo;
    private final ApprovalTemplateValidator validator;

    public ApprovalTemplateService(
            ApprovalTemplateRepository templateRepo,
            ApprovalTemplateValidator validator) {
        this.templateRepo = templateRepo;
        this.validator = validator;
    }

    // CREATE
    public ApprovalTemplateResponse create(ApprovalTemplateRequest req) {
        validator.validate(req);

        ApprovalTemplate template = new ApprovalTemplate();
        template.setName(req.name());
        template.setDescription(req.description());

        for (ApprovalLevelRequest l : req.levels()) {
            template.getLevels().add(toEntity(l, template));
        }

        return toResponse(templateRepo.save(template));
    }

    // UPDATE (safe update)
    public ApprovalTemplateResponse update(UUID id, ApprovalTemplateRequest req) {
        validator.validate(req);

        ApprovalTemplate template = templateRepo.findById(id)
                .orElseThrow();

        template.setName(req.name());
        template.setDescription(req.description());

        template.getLevels().clear();
        for (ApprovalLevelRequest l : req.levels()) {
            template.getLevels().add(toEntity(l, template));
        }

        return toResponse(template);
    }

    // DISABLE (preferred over delete)
    public void disable(UUID id) {
        ApprovalTemplate template = templateRepo.findById(id)
                .orElseThrow();
        template.setActive(false);
    }

    // LIST
    @Transactional
    public List<ApprovalTemplateResponse> listActive() {
        return templateRepo.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // DETAILS
    @Transactional
    public ApprovalTemplateResponse get(UUID id) {
        return templateRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow();
    }

    // --- helpers ---

    private ApprovalLevel toEntity(
            ApprovalLevelRequest r, ApprovalTemplate template) {

        ApprovalLevel l = new ApprovalLevel();
        l.setTemplate(template);
        l.setLevelOrder(r.levelOrder());
        l.setApproverType(r.approverType());
        l.setApprover(r.approver());
        l.setSlaDuration(r.slaDuration());
        l.setReminderBefore(r.reminderBefore());
        l.setEscalationRole(r.escalationRole());
        return l;
    }

    private ApprovalTemplateResponse toResponse(ApprovalTemplate t) {
        return new ApprovalTemplateResponse(
            t.getId(),
            t.getName(),
            t.getDescription(),
            t.isActive(),
            t.getLevels().stream()
                .map(l -> new ApprovalLevelResponse(
                    l.getLevelOrder(),
                    l.getApproverType(),
                    l.getApprover(),
                    l.getSlaDuration(),
                    l.getReminderBefore(),
                    l.getEscalationRole()))
                .toList()
        );
    }
}
