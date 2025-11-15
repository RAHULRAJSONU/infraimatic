package com.ezyinfra.product.nlu.service;

import com.ezyinfra.product.infra.entity.TemplateDefinitionEntity;
import com.ezyinfra.product.infra.repository.TemplateDefinitionRepository;
import com.ezyinfra.product.nlu.dto.ParseByTypeRequest;
import com.ezyinfra.product.nlu.dto.ParseByTypeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParseByTypeService {
    private final TemplateDefinitionRepository templateRepo;
    private final PromptBuilder promptBuilder;
    private final LLMClient llm;
    private final JsonSchemaValidatorService validator;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int maxRetries;
    private final double confidenceThreshold;

    public ParseByTypeService(TemplateDefinitionRepository templateRepo,
                              PromptBuilder promptBuilder,
                              LLMClient llm,
                              JsonSchemaValidatorService validator,
                              @Value("${llm.max-retries:2}") int maxRetries,
                              @Value("${llm.confidence-threshold:0.9}") double confidenceThreshold) {
        this.templateRepo = templateRepo;
        this.promptBuilder = promptBuilder;
        this.llm = llm;
        this.validator = validator;
        this.maxRetries = maxRetries;
        this.confidenceThreshold = confidenceThreshold;
    }

    public ParseByTypeResponse handle(String tenant, ParseByTypeRequest req) throws Exception {
        List<TemplateDefinitionEntity> latestTemplate = templateRepo.findByTenantIdAndTypeOrderByVersionDesc(tenant, req.type);
        if(latestTemplate.isEmpty()){
            throw new IllegalArgumentException("Unknown template type: " + req.type);
        }
        JsonNode template = latestTemplate.get(0).getJsonSchema();

        // minimal schema – for brevity keep the full schema as-is (can minimize if desired)
        JsonNode minimal = template;

        // prompt
        String prompt = promptBuilder.buildInitialPrompt(minimal, req.text,
                List.of("Megha Rastogi arriving 11 Nov 11:00 with laptop SN LAP-HP-2025-7788"),
                req.options);


        String raw = llm.complete(prompt, req.options == null ? Map.of() : req.options);

        JsonNode parsed = extractJson(raw);
        Map<String,Double> conf = extractConfidence(parsed);
        ((ObjectNode)parsed).remove("_confidence");

        Set<ValidationMessage> v = validator.validate(template, parsed);
        List<String> errors = v.stream().map(ValidationMessage::getMessage).collect(Collectors.toList());

        int attempt = 0;
        while (!errors.isEmpty() && attempt < maxRetries) {
            List<String> missing = errors;
            String focused = promptBuilder.buildFocusedPrompt(template, req.text, missing, parsed);
            String raw2 = llm.complete(focused, req.options == null ? Map.of() : req.options);
            JsonNode patch = extractJson(raw2);
            // merge
            parsed = merge(parsed, patch);
            conf.putAll(extractConfidence(patch));
            ((ObjectNode)parsed).remove("_confidence");
            v = validator.validate(template, parsed);
            errors = v.stream().map(ValidationMessage::getMessage).collect(Collectors.toList());
            raw = raw2;
            attempt++;
        }

        boolean lowConfidence = requiredFieldsLowConfidence(parsed, conf, template);
        Set<ValidationMessage> finalValidation = validator.validate(template, parsed);
        List<String> finalErrors = finalValidation.stream().map(ValidationMessage::getMessage).collect(Collectors.toList());
        boolean needsReview = lowConfidence || !finalErrors.isEmpty();

        return new ParseByTypeResponse(parsed, conf, finalErrors, raw, List.of());
    }

    private JsonNode extractJson(String raw) throws Exception {
        int s = raw.indexOf('{'), e = raw.lastIndexOf('}');
        if (s < 0 || e < 0 || e <= s) throw new IllegalArgumentException("No JSON found in model output");
        return mapper.readTree(raw.substring(s, e+1));
    }

    private Map<String,Double> extractConfidence(JsonNode node) {
        Map<String,Double> out = new HashMap<>();
        if (node.has("_confidence")) {
            node.get("_confidence").fieldNames().forEachRemaining(fn -> out.put(fn, node.get("_confidence").get(fn).asDouble()));
        }
        return out;
    }

    private JsonNode merge(JsonNode base, JsonNode patch) {
        ObjectNode b = base.deepCopy();
        patch.fields().forEachRemaining(e -> b.set(e.getKey(), e.getValue()));
        return b;
    }

    private boolean requiredFieldsLowConfidence(JsonNode parsed, Map<String,Double> conf, JsonNode template) {
        if (!template.has("required")) return false;
        for (JsonNode r : template.get("required")) {
            String name = r.asText();
            if (!parsed.has(name)) return true;
            Double c = conf.getOrDefault(name, 0.0);
            if (c < confidenceThreshold) return true;
        }
        return false;
    }
}