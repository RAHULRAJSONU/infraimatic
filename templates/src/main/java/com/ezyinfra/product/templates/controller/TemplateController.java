package com.ezyinfra.product.templates.controller;

import com.ezyinfra.product.common.dto.TemplateCreateRequest;
import com.ezyinfra.product.common.dto.TemplateDto;
import com.ezyinfra.product.templates.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for templates. Paths are always
 * tenant scoped and template version is part of the URL to allow multiple
 * coexisting versions.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Templates", description = "Manage templates per tenant")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/templates/{type}/{version}")
    @Operation(summary = "Get a specific version of a template")
    public ResponseEntity<TemplateDto> getTemplate(
            @PathVariable("type") String type,
            @PathVariable("version") Integer version) {
        TemplateDto dto = templateService.getTemplate(type, version);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/templates/{type}")
    @Operation(summary = "List all versions of a template type for a tenant")
    public ResponseEntity<List<TemplateDto>> listTemplates(
            @PathVariable("type") String type) {
        return ResponseEntity.ok(templateService.listTemplates(type));
    }

    @PostMapping("/templates/{type}")
    @Operation(summary = "Create a new version of a template")
    public ResponseEntity<TemplateDto> createTemplate(
            @PathVariable("type") String type,
            @RequestBody TemplateCreateRequest request) {
        TemplateDto dto = templateService.createTemplate(type, request.name(), request.jsonSchema());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
