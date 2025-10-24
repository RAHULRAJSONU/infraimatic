package com.ezyinfra.product.nlu.service;

import com.ezyinfra.product.common.dto.NluParseResponse;
import com.ezyinfra.product.common.dto.NluSubmitResponse;

/**
 * Service contract for natural language enrichment. Implementations of this
 * interface take free text (and optionally attachments) and return a
 * normalized payload along with confidence scores. Submit operations
 * persist the normalized submission for the tenant and template.
 */
public interface EnrichmentService {
    /**
     * Parse free text into a structured representation. Does not persist.
     *
     * @param tenantId tenant performing the parse
     * @param text     free form text describing the event
     * @return parse response containing normalized payload and metadata
     */
    NluParseResponse parse(String tenantId, String text);

    /**
     * Parse free text and persist the resulting normalized submission. Returns
     * the id of the stored submission and the normalized payload. This method
     * may throw validation exceptions if the normalized data does not match
     * the template's JSON Schema.
     *
     * @param tenantId tenant performing the submission
     * @param text     free form text describing the event
     * @return response containing submission id and normalized payload
     */
    NluSubmitResponse submit(String tenantId, String text);
}