package com.ezyinfra.product.nlu.router;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WorkflowRouter {

    private final GatepassWorkflow gatepassWorkflow;

    public WorkflowRouter(GatepassWorkflow gatepassWorkflow) {
        this.gatepassWorkflow = gatepassWorkflow;
    }

    public String route(UserSession session, Map<String, String> event) {

        if (session.getWorkflow() == WorkflowType.GATEPASS) {
            return gatepassWorkflow.handle(session, event);
        }

        throw new IllegalStateException("No workflow selected");
    }
}
