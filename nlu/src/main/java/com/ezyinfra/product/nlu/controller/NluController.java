package com.ezyinfra.product.nlu.controller;

import com.ezyinfra.product.common.dto.NluParseRequest;
import com.ezyinfra.product.common.dto.NluParseResponse;
import com.ezyinfra.product.common.dto.NluSubmitRequest;
import com.ezyinfra.product.common.dto.NluSubmitResponse;
import com.ezyinfra.product.nlu.service.EnrichmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for natural language understanding endpoints. Delegates
 * requests to the {@link EnrichmentService}.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Tag(name = "NLU", description = "Natural language parsing and submission endpoints")
public class NluController {

    private final EnrichmentService enrichmentService;

    @PostMapping(value = "/{tenantId}/nlu/parse")
    @Operation(summary = "Parse natural language into structured payload")
    public ResponseEntity<NluParseResponse> parse(@PathVariable("tenantId") String tenantId,
                                                  @RequestBody @Valid NluParseRequest request) {
        NluParseResponse response = enrichmentService.parse(tenantId, request.text());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{tenantId}/nlu/submit")
    @Operation(summary = "Parse and persist natural language submission")
    public ResponseEntity<NluSubmitResponse> submit(@PathVariable("tenantId") String tenantId,
                                                    @RequestBody @Valid NluSubmitRequest request) {
        NluSubmitResponse response = enrichmentService.submit(tenantId, request.text());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}