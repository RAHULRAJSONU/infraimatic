package com.ezyinfra.product.nlu.workflow;

import com.ezyinfra.product.nlu.workflow.router.UserSession;
import com.ezyinfra.product.nlu.workflow.router.WorkflowType;

import java.util.Map;

public interface WorkflowHandler {

    WorkflowType getType();

    /**
     * Handle ONE user turn.
     */
    String handle(UserSession session, Map<String, String> event);
}
