package com.ezyinfra.product.infraimatic.web;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalTemplateRequest;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTemplateResponse;
import com.ezyinfra.product.infraimatic.service.ApprovalTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/approval-templates")
public class ApprovalTemplateController {

    private final ApprovalTemplateService service;

    public ApprovalTemplateController(ApprovalTemplateService service) {
        this.service = service;
    }

    @PostMapping
    public ApprovalTemplateResponse create(
            @RequestBody ApprovalTemplateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public ApprovalTemplateResponse update(
            @PathVariable UUID id,
            @RequestBody ApprovalTemplateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void disable(@PathVariable UUID id) {
        service.disable(id);
    }

    @GetMapping
    public List<ApprovalTemplateResponse> list() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public ApprovalTemplateResponse get(@PathVariable UUID id) {
        return service.get(id);
    }
}
