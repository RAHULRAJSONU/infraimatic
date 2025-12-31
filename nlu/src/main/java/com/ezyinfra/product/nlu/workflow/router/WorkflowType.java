package com.ezyinfra.product.nlu.workflow.router;

public enum WorkflowType {
    GATEPASS(1),
    EXPENSE(2),
    INVOICE(3);

    private final int option;

    WorkflowType(int option) {
        this.option = option;
    }

    public static WorkflowType fromUserInput(String input) {
        if (input == null) return null;

        input = input.trim().toLowerCase();

        // numeric selection
        for (WorkflowType type : values()) {
            if (String.valueOf(type.option).equals(input)) {
                return type;
            }
        }

        // keyword fallback (optional)
        if (input.contains("gate")) return GATEPASS;
        if (input.contains("expense")) return EXPENSE;
        if (input.contains("invoice")) return INVOICE;

        return null;
    }
}
