package com.ezyinfra.product.nlu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class JsonSchemaValidatorService {
    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    public Set<ValidationMessage> validate(JsonNode schemaNode, JsonNode data) {
        JsonSchema schema = factory.getSchema(schemaNode);
        return schema.validate(data);
    }
}