package com.ezyinfra.product.nlu.controller;

import com.ezyinfra.product.common.dto.NluParseRequest;
import com.ezyinfra.product.common.dto.NluParseResponse;
import com.ezyinfra.product.common.dto.NluSubmitRequest;
import com.ezyinfra.product.common.dto.NluSubmitResponse;
import com.ezyinfra.product.nlu.dto.ParseByTypeRequest;
import com.ezyinfra.product.nlu.dto.ParseByTypeResponse;
import com.ezyinfra.product.nlu.service.EnrichmentService;
import com.ezyinfra.product.nlu.service.ParseByTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for natural language understanding endpoints. Delegates
 * requests to the {@link EnrichmentService}.
 */
@RestController
@RequestMapping("/api/v1/nlu")
@RequiredArgsConstructor
@Validated
@Tag(name = "NLU", description = "Natural language parsing")
public class NluController {

    private final ParseByTypeService typeParser;

    @PostMapping(value = "/entry/{tenantId}/{type}")
    @Operation(summary = "Parse natural language into structured payload")
    public ResponseEntity<ParseByTypeResponse> parse(@PathVariable("tenantId") String tenantId,
                                                  @PathVariable("type") String type,
                                                  @RequestBody ParseByTypeRequest req) {
        try {
            ParseByTypeResponse response = typeParser.handle(tenantId, req);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ParseByTypeResponse(null, Map.of(), List.of(ex.getMessage()), "", List.of()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ParseByTypeResponse(null, Map.of(), List.of("internal error"), "", List.of(e.getMessage())));
        }
    }

}