package com.ezyinfra.product.nlu.service.impl;

import com.ezyinfra.product.nlu.dto.ExtractRequest;
import com.ezyinfra.product.nlu.dto.QuestionRequest;
import com.ezyinfra.product.nlu.service.FieldQuestionRegistry;
import com.ezyinfra.product.nlu.service.JsonExtractor;
import com.ezyinfra.product.nlu.service.LLMClient;
import com.ezyinfra.product.nlu.service.ParseByTypeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ParseByTypeServiceImpl implements ParseByTypeService {

    private final LLMClient llmClient;
    private final JsonExtractor jsonExtractor;
    private final FieldQuestionRegistry fQRegistry;

    public ParseByTypeServiceImpl(
            LLMClient llmClient,
            ObjectMapper objectMapper, FieldQuestionRegistry fqRegistry
    ) {
        this.llmClient = llmClient;
        this.jsonExtractor = new JsonExtractor(objectMapper);
        fQRegistry = fqRegistry;
    }

    // ------------------------------------------------------------------
    // 1️⃣ STRICT EXTRACTION (NO QUESTIONS)
    // ------------------------------------------------------------------

    @Override
    public JsonNode extractPartialPatch(ExtractRequest req) {

        String prompt = buildPartialPatchPrompt(
                req.fullSchema(),
                req.userText(),
                req.existingJson(),
                req.targetFields(),
                req.alreadyAskedFields(),
                req.requiredFields()
        );

        log.debug("Extraction prompt:\n{}", prompt);

        String raw = llmClient.complete(prompt, req.options());
        log.info("Raw LLM output:\n{}", raw);

        return jsonExtractor.safeParseOrEmpty(raw);
    }

    // ------------------------------------------------------------------
    // 2️⃣ QUESTION GENERATION (LLM-BASED)
    // ------------------------------------------------------------------

    @Override
    public String generateQuestions(QuestionRequest req) {
        log.info("Generating questions for, request: {}", req);
        List<String> questions = req.pendingFields().stream()
                .filter(f -> {
                    boolean fieldCollected = false;
                    try {
                        JsonNode fieldNode = req.collectedJson().get(f);
                        if (fieldNode != null) {
                            fieldCollected = fieldNode.asText() != null;
                        }
                        log.info("Field: {}, collected: {}.", f, fieldCollected);
                    }catch (Exception _){}
                    return !fieldCollected;
                })
                .map(fQRegistry::questionFor)
                .distinct()
                .toList();
        if (questions.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Please provide the following details:\n");

        int i = 1;
        for (String q : questions) {
            sb.append(i++).append(". ").append(q).append("\n");
        }

        return sb.toString();
    }

    // ------------------------------------------------------------------
    // PROMPT BUILDERS
    // ------------------------------------------------------------------

    public String buildPartialPatchPrompt(
            JsonNode fullSchema,
            String userText,
            JsonNode existingJson,
            Set<String> targetFields,
            Set<String> alreadyAskedFields,
            Set<String> requiredFields
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            You are a STRICT INFORMATION EXTRACTION ENGINE.
            You are NOT a chatbot.
            You MUST NOT ask questions.
            You MUST NOT explain anything.
            
            ====================
            ABSOLUTE RULES
            ====================
            - Output VALID JSON only
            - No markdown
            - No explanations
            - No inferred or guessed values
            - Extract ONLY what is EXPLICITLY stated by the user
            - Do NOT repeat existing values unless explicitly updated
            - If information is missing, OMIT the field
            - NEVER fabricate values
            - NEVER ask follow-up questions
            - Returning {} is VALID
            """);

        sb.append("\n====================\nSCHEMA\n====================\n");
        sb.append(fullSchema.toPrettyString()).append("\n");

        sb.append("\n====================\nREQUIRED_FIELDS\n====================\n");
        sb.append(requiredFields.toString()).append("\n");

        sb.append("\n====================\nCURRENT_STATE_JSON\n====================\n");
        sb.append(existingJson == null ? "{}" : existingJson.toPrettyString()).append("\n");

        sb.append("\n====================\nALREADY_ASKED_FIELDS\n====================\n");
        sb.append(alreadyAskedFields == null ? "[]" : alreadyAskedFields).append("\n");

        if (targetFields != null && !targetFields.isEmpty()) {
            sb.append("\n====================\nTARGET_FIELDS\n====================\n");
            sb.append(targetFields).append("\n");
        }

        sb.append("\n====================\nUSER_MESSAGE\n====================\n");
        sb.append("\"\"\"\n").append(userText).append("\n\"\"\"\n");

        sb.append("""
            ====================
            OUTPUT FORMAT
            ====================
            Return ONLY a JSON object.
            Include a top-level "_confidence" object ONLY for extracted fields.
            If nothing is extracted, return {}.
            """);

        return sb.toString();
    }
}
