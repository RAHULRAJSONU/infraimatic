package com.ezyinfra.product.nlu.workflow.router;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

import java.util.HashSet;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    /** Unique user identifier (WhatsApp number, etc.) */
    private String phone;

    /** Active workflow type (GATEPASS / EXPENSE / etc.) */
    private WorkflowType workflow;

    /** Workflow lifecycle state */
    private WorkflowState state; // IN_PROGRESS / COMPLETED

    /**
     * Accumulated structured JSON.
     * This is the SINGLE source of truth.
     */
    @Builder.Default
    private JsonNode collectedData = null;

    /**
     * Schema-derived required fields that are still missing.
     * Recomputed every turn (not conversational memory).
     */
    private Set<String> pendingFields;

    /**
     * Fields that have already been asked to the user.
     * This PREVENTS repeated questions and infinite loops.
     */
    private Set<String> askedFields;

    /**
     * Optional audit/debug log.
     * NEVER sent to LLM.
     */
    private StringBuilder auditLog;

    public UserSession(String phone) {
        this.phone = phone;
        this.state = WorkflowState.IN_PROGRESS;
        this.askedFields = new HashSet<>();
        this.pendingFields = new LinkedHashSet<>();
        this.auditLog = new StringBuilder();
    }

    // --------------------------------------------------
    // Utility helpers (safe & optional)
    // --------------------------------------------------

    public void logTurn(String userText) {
        auditLog.append("USER: ").append(userText).append("\n");
    }

    public void logSystem(String message) {
        auditLog.append("SYSTEM: ").append(message).append("\n");
    }

    public void reset() {
        phone = null;
        workflow = null;
        state = WorkflowState.IN_PROGRESS;
        collectedData = null;
        if(!pendingFields.isEmpty()) pendingFields.clear();
        if(!askedFields.isEmpty())askedFields.clear();
    }

}
