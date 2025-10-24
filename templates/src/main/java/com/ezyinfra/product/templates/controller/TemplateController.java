package com.ezyinfra.product.templates.controller;

import com.ezyinfra.product.common.dto.TemplateCreateRequest;
import com.ezyinfra.product.common.dto.TemplateDto;
import com.ezyinfra.product.templates.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for templates. Paths are always
 * tenant scoped and template version is part of the URL to allow multiple
 * coexisting versions.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Tag(name = "Templates", description = "Manage templates per tenant")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/{tenantId}/templates/{type}/{version}")
    @Operation(summary = "Get a specific version of a template")
    public ResponseEntity<TemplateDto> getTemplate(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("type") String type,
            @PathVariable("version") Integer version) {
        TemplateDto dto = templateService.getTemplate(tenantId, type, version);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{tenantId}/templates/{type}")
    @Operation(summary = "List all versions of a template type for a tenant")
    public ResponseEntity<List<TemplateDto>> listTemplates(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("type") String type) {
        return ResponseEntity.ok(templateService.listTemplates(tenantId, type));
    }

    @PostMapping("/{tenantId}/templates/{type}")
    @Operation(summary = "Create a new version of a template")
    public ResponseEntity<TemplateDto> createTemplate(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("type") String type,
            @RequestBody @Valid TemplateCreateRequest request) {
        TemplateDto dto = templateService.createTemplate(tenantId, type, request.name(), request.jsonSchema());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
