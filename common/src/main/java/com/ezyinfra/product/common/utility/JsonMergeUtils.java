package com.ezyinfra.product.common.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

public final class JsonMergeUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonMergeUtils() {}

    /**
     * Deep merges a JSON patch into a base JSON object.
     *
     * Rules:
     * - Only fields present in patch are merged
     * - Objects are merged recursively
     * - Scalars and arrays overwrite
     * - Null in patch explicitly overwrites
     * - _confidence is ignored
     */
    public static JsonNode merge(JsonNode base, JsonNode patch) {

        if (patch == null || patch.isNull()) {
            return base;
        }

        ObjectNode result;

        if (base == null || base.isNull()) {
            result = MAPPER.createObjectNode();
        } else {
            result = base.deepCopy();
        }

        mergeInto(result, patch);
        return result;
    }

    // ------------------------------------------------------------------

    private static void mergeInto(ObjectNode target, JsonNode patch) {

        Iterator<Map.Entry<String, JsonNode>> fields = patch.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String field = entry.getKey();
            JsonNode patchValue = entry.getValue();

            // Skip confidence metadata
            if ("_confidence".equals(field)) {
                continue;
            }

            JsonNode existingValue = target.get(field);

            // Recursive object merge
            if (existingValue != null
                    && existingValue.isObject()
                    && patchValue.isObject()) {

                mergeInto((ObjectNode) existingValue, patchValue);
            }
            // Overwrite (including explicit nulls)
            else {
                target.set(field, patchValue);
            }
        }
    }
}
