package com.ezyinfra.product.infraimatic.exception;

public final class ProblemTypes {

    private static final String BASE =
            "https://api.example.com/problems/";

    public static final String APPROVAL_NOT_FOUND =
            BASE + "approval-not-found";

    public static final String NOT_AUTHORIZED =
            BASE + "not-authorized";

    public static final String ENTITY_LOCKED =
            BASE + "entity-locked";

    public static final String INVALID_STATE =
            BASE + "invalid-state";

    public static final String CONCURRENT_MODIFICATION =
            BASE + "concurrent-modification";

    public static final String VALIDATION_ERROR =
            BASE + "validation-error";

    public static final String INTERNAL_ERROR =
            BASE + "internal-error";

    private ProblemTypes() {}
}
