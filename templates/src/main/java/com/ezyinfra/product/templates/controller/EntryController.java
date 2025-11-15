package com.ezyinfra.product.templates.controller;

import com.ezyinfra.product.common.dto.EntryDto;
import com.ezyinfra.product.templates.service.EntryService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller providing dynamic CRUD endpoints for entries. Entries
 * are scoped by tenant, template type and template version.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Tag(name = "Entry", description = "Manage entries per tenant and template")
public class EntryController {

    private final EntryService entryService;

    @PostMapping("/entry/{tenantId}/{templateType}/{version}")
    @Operation(summary = "Create a new entry with a normalized payload")
    public ResponseEntity<EntryDto> createEntry(@PathVariable("tenantId") String tenantId,
                                                @PathVariable("templateType") String templateType,
                                                @PathVariable("version") Integer version,
                                                @RequestBody @Valid JsonNode normalized) {
        EntryDto dto = entryService.createEntry(tenantId, templateType, version, normalized, null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/entry/{tenantId}/{templateType}/{version}/{id}")
    @Operation(summary = "Get a entry by id")
    public ResponseEntity<EntryDto> getEntry(@PathVariable("tenantId") String tenantId,
                                             @PathVariable("templateType") String templateType,
                                             @PathVariable("id") UUID id) {
        return ResponseEntity.ok(entryService.getEntry(tenantId, templateType, id));
    }

    @GetMapping("/entry/{tenantId}/{templateType}")
    @Operation(summary = "List entries for a template type (latest version)")
    public ResponseEntity<Page<EntryDto>> listEntriesWithoutVersion(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("templateType") String templateType,
            Pageable pageable) {

        return ResponseEntity.ok(entryService.listEntriesPageable(tenantId, templateType, pageable));
    }

    @GetMapping("/entry/{tenantId}/{templateType}/{version}")
    @Operation(summary = "List entries for a template type and specific version")
    public ResponseEntity<Page<EntryDto>> listEntriesWithVersion(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("templateType") String templateType,
            @PathVariable("version") Integer version,
            Pageable pageable) {

        return ResponseEntity.ok(entryService.listEntriesPageable(tenantId, templateType, version, pageable));
    }

}