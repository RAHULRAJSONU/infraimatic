package com.ezyinfra.product.nlu.workflow.gatepass;

import com.ezyinfra.product.common.dto.TemplateDto;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.ezyinfra.product.common.utility.JsonMergeUtils;
import com.ezyinfra.product.common.utility.JsonSchemaUtils;
import com.ezyinfra.product.nlu.dto.ExtractRequest;
import com.ezyinfra.product.nlu.dto.QuestionRequest;
import com.ezyinfra.product.nlu.service.ParseByTypeService;
import com.ezyinfra.product.nlu.workflow.router.UserSession;
import com.ezyinfra.product.nlu.workflow.router.WorkflowFieldUtils;
import com.ezyinfra.product.nlu.workflow.router.WorkflowState;
import com.ezyinfra.product.nlu.workflow.router.WorkflowType;
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

    private final ConcurrentHashMap<String, Set<String>> requiredFieldsCache =
            new ConcurrentHashMap<>();

    public GatepassWorkflow(
            ObjectMapper objectMapper,
            ParseByTypeService parseByTypeService,
            TemplateService templateService,
            EntryService entryService
    ) {
        this.objectMapper = objectMapper;
        this.parseByTypeService = parseByTypeService;
        this.templateService = templateService;
        this.entryService = entryService;
    }

    // --------------------------------------------------
    // MAIN WORKFLOW TURN
    // --------------------------------------------------

    public String handle(UserSession session, Map<String, String> event) {

        String userText = event.get("Body");
        log.info("GatepassWorkflow.handle | text='{}'", userText);

        session.setWorkflow(WorkflowType.GATEPASS);
        session.setState(WorkflowState.IN_PROGRESS);

        // 1️⃣ Load effective required fields
        Set<String> requiredFields = loadEffectiveRequiredFields();

        // 2️⃣ Extract partial patch (STRICT, NO QUESTIONS)
        JsonNode patch = parseByTypeService.extractPartialPatch(
                ExtractRequest.builder()
                        .type(TYPE)
                        .userText(userText)
                        .requiredFields(requiredFields)
                        .existingJson(session.getCollectedData())
                        .fullSchema(templateService.getLatestTemplate(TYPE).jsonSchema())
                        .targetFields(session.getPendingFields())
                        .alreadyAskedFields(session.getAskedFields())
                        .build()
        );

        // 3️⃣ Merge patch safely
        JsonNode merged = JsonMergeUtils.merge(session.getCollectedData(), patch);
        session.setCollectedData(merged);

        // 4️⃣ Recompute pending fields
        Set<String> pending =
                WorkflowFieldUtils.findMissingRequired(requiredFields, merged);
        session.setPendingFields(pending);

        // 5️⃣ Complete or ask remaining questions
        if (pending.isEmpty()) {
            return completeWorkflow(session);
        }

        // 6️⃣ Ask ALL remaining fields ONCE
        String questions = parseByTypeService.generateQuestions(
                QuestionRequest.builder()
                        .type(TYPE)
                        .requiredFields(requiredFields)
                        .pendingFields(pending)
                        .alreadyAskedFields(session.getAskedFields())
                        .collectedJson(merged)
                        .build()
        );

        // 7️⃣ Mark asked fields to prevent loops
        session.getAskedFields().addAll(pending);
        log.info("session.getAskedFields(): {}, question: {}",session, questions);
        return questions;
    }

    // --------------------------------------------------
    // COMPLETION
    // --------------------------------------------------

    private String completeWorkflow(UserSession session) {
        try {
            session.setState(WorkflowState.COMPLETED);

            JsonNode normalized =
                    objectMapper.readTree(session.getCollectedData().toPrettyString());

            entryService.createEntry(
                    TYPE,
                    normalized,
                    session.getCollectedData(),
                    null
            );

            return """
                ✅ Gate pass details captured successfully and sent for approval.
                %s
                """.formatted(normalized.toPrettyString());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to complete gatepass", e);
        }
    }

    // --------------------------------------------------
    // REQUIRED FIELD CACHE
    // --------------------------------------------------

    private Set<String> loadEffectiveRequiredFields() {
        return requiredFieldsCache.computeIfAbsent(TYPE, k -> {
            TemplateDto template = templateService.getLatestTemplate(TYPE);
            if (template == null) {
                throw new NotFoundException("Template GATEPASS not found");
            }
            return JsonSchemaUtils.extractEffectivelyRequiredFields(
                    template.jsonSchema()
            );
        });
    }
}

