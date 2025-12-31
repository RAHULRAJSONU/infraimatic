package com.ezyinfra.product.nlu.workflow;

import java.util.Set;

public final class WorkflowCommandParser {

    private static final Set<String> RESET_KEYWORDS =
            Set.of("reset", "cancel", "start over", "clear", "restart");

    private static final Set<String> SWITCH_KEYWORDS =
            Set.of("switch", "change", "new", "instead");

    public static WorkflowCommand detect(String input) {
        if (input == null) return WorkflowCommand.NONE;

        String text = input.toLowerCase();

        if (RESET_KEYWORDS.stream().anyMatch(text::contains)) {
            return WorkflowCommand.RESET;
        }

        if (SWITCH_KEYWORDS.stream().anyMatch(text::contains)) {
            return WorkflowCommand.SWITCH;
        }

        return WorkflowCommand.NONE;
    }
}
