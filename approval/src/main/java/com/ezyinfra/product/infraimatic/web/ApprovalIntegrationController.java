package com.ezyinfra.product.infraimatic.web;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalIntegrationRequest;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalIntegrationConfig;
import com.ezyinfra.product.infraimatic.service.ApprovalIntegrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/approval-integrations")
public class ApprovalIntegrationController {

    private final ApprovalIntegrationService service;

    public ApprovalIntegrationController(ApprovalIntegrationService service) {
        this.service = service;
    }

    @PostMapping
    public ApprovalIntegrationConfig save(
            @RequestBody ApprovalIntegrationRequest r) {
        return service.save(r);
    }
}
