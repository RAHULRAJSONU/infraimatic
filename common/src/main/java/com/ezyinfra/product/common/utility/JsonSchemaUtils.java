package com.ezyinfra.product.common.utility;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public final class JsonSchemaUtils {

    private JsonSchemaUtils() {}

    // ------------------------------------------------------------------
    // EXISTING METHOD (UNCHANGED)
    // ------------------------------------------------------------------

    /**
     * Recursively extracts all explicit JSON-Schema required fields.
     * Nested fields are returned using dot-notation (e.g. requestor.name).
     */
    public static Set<String> getAllRequiredFields(JsonNode schemaNode) {
        Set<String> result = new LinkedHashSet<>();
        collectExplicitRequired(schemaNode, "", result);
        return result;
    }

    private static void collectExplicitRequired(JsonNode node, String path, Set<String> result) {
        if (node == null || !node.isObject()) {
            return;
        }

        if (node.has("required") && node.get("required").isArray()) {
            for (JsonNode req : node.get("required")) {
                if (req.isTextual()) {
                    String field = req.asText();
                    result.add(path.isEmpty() ? field : path + "." + field);
                }
            }
        }

        if (node.has("properties") && node.get("properties").isObject()) {
            Iterator<String> it = node.get("properties").fieldNames();
            while (it.hasNext()) {
                String field = it.next();
                collectExplicitRequired(
                        node.get("properties").get(field),
                        path.isEmpty() ? field : path + "." + field,
                        result
                );
            }
        }

        if (node.has("items")) {
            collectExplicitRequired(node.get("items"), path + "[]", result);
        }
    }

    // ------------------------------------------------------------------
    // NEW METHOD (USED BY WORKFLOW ENGINE)
    // ------------------------------------------------------------------

    public static Set<String> extractEffectivelyRequiredFields(JsonNode schemaNode) {
        Set<String> result = new LinkedHashSet<>();
        collectEffectiveRequired(schemaNode, "", result);
        return result;
    }

    /**
     * Extracts "effectively required" fields for conversational workflows.
     *
     * A field is effectively required if:
     *  - It is explicitly listed in "required"
     *  - OR its type does NOT allow null
     *
     * Nested paths are returned using dot-notation.
     */
    public static Set<String> extractEffectivelyRequiredFields_v1(JsonNode schemaNode) {
        Set<String> result = new LinkedHashSet<>();
        collectEffectiveRequired_v1(schemaNode, "", result);
        return result;
    }

    private static void collectEffectiveRequired(
            JsonNode node,
            String path,
            Set<String> result
    ) {
        if (node == null || !node.isObject()) {
            return;
        }

        // 1️⃣ Explicit JSON-Schema "required"
        if (node.has("required") && node.get("required").isArray()) {
            for (JsonNode req : node.get("required")) {
                if (req.isTextual()) {
                    String field = req.asText();
                    result.add(path.isEmpty() ? field : path + "." + field);
                }
            }
        }
    }

    private static void collectEffectiveRequired_v1(
            JsonNode node,
            String path,
            Set<String> result
    ) {
        if (node == null || !node.isObject()) {
            return;
        }

        // 1️⃣ Explicit JSON-Schema "required"
        if (node.has("required") && node.get("required").isArray()) {
            for (JsonNode req : node.get("required")) {
                if (req.isTextual()) {
                    String field = req.asText();
                    result.add(path.isEmpty() ? field : path + "." + field);
                }
            }
        }

        // 2️⃣ Properties that do NOT allow null
        if (node.has("properties") && node.get("properties").isObject()) {
            Iterator<String> it = node.get("properties").fieldNames();
            while (it.hasNext()) {
                String field = it.next();
                JsonNode prop = node.get("properties").get(field);
                String fullPath = path.isEmpty() ? field : path + "." + field;

                if (isNonNullable(prop)) {
                    result.add(fullPath);
                }

                // recurse
                collectEffectiveRequired(prop, fullPath, result);
            }
        }

        // 3️⃣ Arrays → inspect items schema
        if (node.has("items")) {
            collectEffectiveRequired(node.get("items"), path + "[]", result);
        }
    }

    private static boolean isNonNullable(JsonNode prop) {
        if (!prop.has("type")) {
            return false;
        }

        JsonNode typeNode = prop.get("type");

        // "type": "string"
        if (typeNode.isTextual()) {
            return !"null".equals(typeNode.asText());
        }

        // "type": ["string","null"]
        if (typeNode.isArray()) {
            for (JsonNode t : typeNode) {
                if ("null".equals(t.asText())) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
