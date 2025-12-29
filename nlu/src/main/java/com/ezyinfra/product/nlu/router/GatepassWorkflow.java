package com.ezyinfra.product.nlu.router;

import com.ezyinfra.product.common.dto.TemplateDto;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.ezyinfra.product.common.utility.JsonMergeUtils;
import com.ezyinfra.product.common.utility.JsonSchemaUtils;
import com.ezyinfra.product.nlu.dto.ParseByTypeRequest;
import com.ezyinfra.product.nlu.service.ParseByTypeService;
import com.ezyinfra.product.templates.service.EntryService;
import com.ezyinfra.product.templates.service.TemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GatepassWorkflow {

    private static final String TYPE = "GATEPASS";
    private final ObjectMapper objectMapper;
    private final ParseByTypeService parseByTypeService;
    private final TemplateService templateService;
    private final EntryService entryService;

    /**
     * Cache of schema-derived required (effective) fields
     */
    private final ConcurrentHashMap<String, Set<String>> requiredFieldsCache =
            new ConcurrentHashMap<>();

    public GatepassWorkflow(ObjectMapper objectMapper, ParseByTypeService parseByTypeService,
                            TemplateService templateService, EntryService entryService) {
        this.objectMapper = objectMapper;
        this.parseByTypeService = parseByTypeService;
        this.templateService = templateService;
        this.entryService = entryService;
    }

    /**
     * Handles one user turn for Gatepass workflow.
     * Returns the message that should be sent back to the user.
     */
    public String handle(UserSession session, Map<String, String> event) {

        log.info("GatepassWorkflow.handle | session={}", session);

        String userText = event.get("Body");
        appendConversation(session, userText);

        session.setWorkflow(WorkflowType.GATEPASS);
        session.setState(WorkflowState.IN_PROGRESS);

        // 1️⃣ Load schema-driven effective required fields
        Set<String> requiredFields = loadEffectiveRequiredFields();
        log.info("Required fields loaded: {}",requiredFields);
        // 2️⃣ Ask LLM to extract a PARTIAL JSON PATCH
        JsonNode patch;
        try {
            patch = parseByTypeService.extractPartial(
                    ParseByTypeRequest.builder()
                            .type(TYPE)
                            .text(userText) // IMPORTANT: only this turn
                            .existingData(session.getCollectedData())
                            .targetFields(session.getPendingFields())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract gatepass data", e);
        }

        // 3️⃣ Merge patch into session JSON
        JsonNode merged =
                JsonMergeUtils.merge(session.getCollectedData(), patch);
        session.setCollectedData(merged);

        // 4️⃣ Detect pending fields dynamically
        Set<String> pending =
                WorkflowFieldUtils.findMissingRequired(requiredFields, merged);
        session.setPendingFields(pending);

        // 5️⃣ Ask next question OR complete workflow
        if (!pending.isEmpty()) {
            return askNextQuestion(session, pending.iterator().next());
        }

        return completeWorkflow(session);
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    private void appendConversation(UserSession session, String userText) {
        String convo = session.getConversationText();
        session.setConversationText(
                convo == null ? userText : convo + "\n" + userText
        );
    }

    private String askNextQuestion(UserSession session, String field) {

        String question = switch (field) {
            case "expectedStart" ->
                    "When will the gatepass start? (date and time)";
            case "expectedEnd" ->
                    "When is the expected end time?";
            case "preferredType" ->
                    "Is this an entry, exit, internal transfer, or temporary exit?";
            case "requestor.name" ->
                    "Who is requesting this gatepass?";
            default ->
                    "Please provide " + field.replace('.', ' ');
        };

        session.setLastQuestion(question);
        return question;
    }

    private String completeWorkflow(UserSession session) {
        try {
            session.setState(WorkflowState.COMPLETED);
            JsonNode normalised = objectMapper.readTree(session.getCollectedData().toPrettyString());
            entryService.createEntry(TYPE, normalised, session.getCollectedData(), null);
            return "✅ Gate pass details captured successfully, and sent for approval.\n"+normalised;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Set<String> loadEffectiveRequiredFields() {
        return requiredFieldsCache.computeIfAbsent(TYPE, k -> {
            TemplateDto template =
                    templateService.getLatestTemplate(TYPE);
            if (template == null) {
                throw new NotFoundException("Template GATEPASS not found");
            }
            // IMPORTANT: effective required, not JSON-schema-only required
            return JsonSchemaUtils.extractEffectivelyRequiredFields(
                    template.jsonSchema()
            );
        });
    }
}
