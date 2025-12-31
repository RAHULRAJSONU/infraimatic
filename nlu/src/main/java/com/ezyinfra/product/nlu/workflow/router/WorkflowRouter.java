package com.ezyinfra.product.nlu.workflow.router;

import com.ezyinfra.product.nlu.workflow.gatepass.GatepassWorkflow;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class WorkflowRouter {

    private final GatepassWorkflow gatepassWorkflow;

    public WorkflowRouter(
            GatepassWorkflow gatepassWorkflow
    ) {
        this.gatepassWorkflow = gatepassWorkflow;
    }

    public String route(UserSession session, Map<String, String> event) {

        String body = event.getOrDefault("Body", "").trim();

        // 1️⃣ If NO workflow yet → try deterministic selection
        if (session.getWorkflow() == null) {

            WorkflowType selected = WorkflowType.fromUserInput(body);

            if (selected != null) {
                session.reset();                  // clean slate
                session.setWorkflow(selected);
                session.setState(WorkflowState.IN_PROGRESS);
                session.logSystem("Workflow selected: " + selected);

                return switch (selected) {
                    case GATEPASS -> "Please provide gatepass details.";
                    case EXPENSE -> "Please provide expense details.";
                    case INVOICE -> "Please provide invoice details.";
                };
            }

            // No selection → show menu
            return menuMessage();
        }

        // 2️⃣ Workflow already locked → route ONLY to that workflow
        return switch (session.getWorkflow()) {
            case GATEPASS -> gatepassWorkflow.handle(session, event);
            default -> throw new IllegalArgumentException("Operation not supported.");
        };
    }

    private String menuMessage() {
        return """
                Choose an option:
                1️⃣ Gatepass
                2️⃣ Expense
                3️⃣ Invoice

                You can also say:
                • create gatepass
                • submit expense
                • upload invoice
                """;
    }
}
