package com.ezyinfra.product.nlu.router;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {

    private String phone;
    private WorkflowType workflow;      // GATEPASS / EXPENSE
    private WorkflowState state;         // IN_PROGRESS / COMPLETED

    /** Accumulated structured JSON */
    private JsonNode collectedData;

    /** Fields still required (schema-driven) */
    @Builder.Default
    private Set<String> pendingFields = new LinkedHashSet<>();

    /** Last question asked to the user */
    private String lastQuestion;

    /** Raw user input history (optional but useful) */
    private String conversationText;

    public UserSession(String phone){
        this.phone = phone;
    }
}
