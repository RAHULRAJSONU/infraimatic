package com.ezyinfra.product.nlu.workflow.router;

import com.ezyinfra.product.nlu.workflow.gatepass.GatepassWorkflow;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class WorkflowRouter {

    private final GatepassWorkflow gatepassWorkflow;
    private final Set<String> resetAlias;

    public WorkflowRouter(
            GatepassWorkflow gatepassWorkflow
    ) {
        this.gatepassWorkflow = gatepassWorkflow;
        this.resetAlias = Set.of("RESET","CANCEL","RESTART","EXIT","STOP","QUIT");
    }

    public String route(UserSession session, Map<String, String> event) {

        String body = event.getOrDefault("Body", "").trim();

        if(resetAlias.contains(body.toUpperCase())){
            session.reset();
            return "Thank you, please type Hi to start the conversation again.";
        }

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
