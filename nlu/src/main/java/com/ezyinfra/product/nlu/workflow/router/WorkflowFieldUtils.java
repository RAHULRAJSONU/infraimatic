package com.ezyinfra.product.nlu.workflow.router;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashSet;
import java.util.Set;

public final class WorkflowFieldUtils {

    private WorkflowFieldUtils() {}

    public static Set<String> findMissingRequired(
            Set<String> requiredFields,
            JsonNode parsed) {

        Set<String> missing = new LinkedHashSet<>();
        for (String field : requiredFields) {
            if (!parsed.has(field) || parsed.get(field).isNull()) {
                missing.add(field);
            }
        }
        return missing;
    }
}