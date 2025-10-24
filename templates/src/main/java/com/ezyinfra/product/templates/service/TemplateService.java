package com.ezyinfra.product.templates.service;

import com.ezyinfra.product.common.dto.TemplateDto;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Service for managing templates. Provides CRUD operations per tenant.
 */
public interface TemplateService {
    /**
     * Create a new version of a template for a tenant. If no previous version exists
     * for the given type, version 1 will be created.
     *
     * @param tenantId   the tenant identifier
     * @param type       the template type (e.g. tt_movement_register)
     * @param name       human friendly name of the template
     * @param jsonSchema canonical JSON Schema describing the normalized payload
     * @return the newly created template definition
     */
    TemplateDto createTemplate(String tenantId, String type, String name, JsonNode jsonSchema);

    /**
     * Retrieve a specific version of a template. If not found, throws.
     *
     * @param tenantId tenant identifier
     * @param type     template type
     * @param version  version number
     * @return the template DTO
     * @throws NotFoundException if the template is not found
     */
    TemplateDto getTemplate(String tenantId, String type, Integer version);

    /**
     * Retrieve the latest version of a template for a tenant.
     *
     * @param tenantId tenant identifier
     * @param type     template type
     * @return the latest template DTO
     * @throws NotFoundException if no template exists for the tenant and type
     */
    TemplateDto getLatestTemplate(String tenantId, String type);

    /**
     * List all versions of a template type for a tenant in descending order.
     */
    List<TemplateDto> listTemplates(String tenantId, String type);
}