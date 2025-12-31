package com.ezyinfra.product.nlu.pipeline;

import com.ezyinfra.product.nlu.workflow.router.UserSession;
import com.ezyinfra.product.storage.model.StorageResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PipelineContext {

    private String requestId;        // UUID for tracing
    private String phone;
    private UserSession session;

    /** Normalized user text (post audio/OCR/etc) */
    private String text;

    /** Raw webhook params */
    private Map<String, String> event;

    /** Stored media */
    private StorageResult media;

    /** Extracted JSON patch */
    private JsonNode patch;

    /** Final response */
    private String response;

    /** Errors captured (never thrown across stages) */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public void fail(String error) {
        errors.add(error);
    }
}

