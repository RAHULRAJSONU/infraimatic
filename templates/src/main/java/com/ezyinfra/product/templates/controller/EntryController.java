package com.ezyinfra.product.templates.controller;

import com.ezyinfra.product.common.dto.EntryDto;
import com.ezyinfra.product.templates.service.EntryService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/{tenantId}/{templateType}/{version}/entry")
    @Operation(summary = "Create a new entry with a normalized payload")
    public ResponseEntity<EntryDto> createEntry(@PathVariable("tenantId") String tenantId,
                                                @PathVariable("templateType") String templateType,
                                                @PathVariable("version") Integer version,
                                                @RequestBody @Valid JsonNode normalized) {
        EntryDto dto = entryService.createEntry(tenantId, templateType, version, normalized, null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{tenantId}/{templateType}/{version}/submissions/{id}")
    @Operation(summary = "Get a entry by id")
    public ResponseEntity<EntryDto> getEntry(@PathVariable("tenantId") String tenantId,
                                             @PathVariable("templateType") String templateType,
                                             @PathVariable("version") Integer version,
                                             @PathVariable("id") Long id) {
        return ResponseEntity.ok(entryService.getEntry(tenantId, templateType, version, id));
    }

    @GetMapping("/{tenantId}/{templateType}/{version}/submissions")
    @Operation(summary = "List entries for a template type and version")
    public ResponseEntity<List<EntryDto>> listEntries(@PathVariable("tenantId") String tenantId,
                                                      @PathVariable("templateType") String templateType,
                                                      @PathVariable("version") Integer version) {
        return ResponseEntity.ok(entryService.listEntries(tenantId, templateType, version));
    }
}