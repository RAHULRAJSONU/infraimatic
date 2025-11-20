package com.ezyinfra.product.templates.service;

import com.ezyinfra.product.common.dto.EntryDto;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.ezyinfra.product.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for ingesting submissions, validating them against
 * template schemas and persisting them. Also supports retrieval of
 * stored submissions.
 */
public interface EntryService {
    /**
     * Ingest a new submission for a given tenant and template version. The
     * normalized payload must conform to the JSON Schema stored against the
     * template definition. If validation fails, a ValidationException is thrown.
     *
     * @param tenantId       tenant identifier
     * @param type           template type
     * @param version        template version
     * @param normalized     the normalized payload conforming to the template's JSON schema
     * @param payload        the original payload (may be null if created via NLU)
     * @param processingMeta additional metadata produced during processing (optional)
     * @return the persisted submission DTO
     * @throws NotFoundException   if the specified template cannot be found
     * @throws ValidationException if the normalized payload fails schema validation
     */
    EntryDto createEntry(String tenantId, String type,
                         JsonNode normalized, JsonNode payload,
                         JsonNode processingMeta);

    /**
     * Retrieve a submission by its identifier. Checks that the submission
     * belongs to the tenant and matches the template type/version.
     *
     * @param tenantId tenant identifier
     * @param type     template type
     * @param version  template version
     * @param id       submission id
     * @return the submission DTO
     * @throws NotFoundException if the submission is not found
     */
    EntryDto getEntry(String tenantId, String type, Integer version, UUID id);

    EntryDto getEntry(String tenantId, String type, UUID id);

    /**
     * List submissions for the given tenant, template type and version. For simplicity
     * this method returns all matching submissions without pagination.
     */
    List<EntryDto> listEntries(String tenantId, String type, Integer version);

    Page<EntryDto> listEntriesPageable(String tenantId, String type, Integer version, Pageable pageable);

    Page<EntryDto> listEntriesPageable(String tenantId, String type, Pageable pageable);
}