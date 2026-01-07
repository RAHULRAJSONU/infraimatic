package com.ezyinfra.product.infraimatic.exception;

import java.time.Instant;

public record ErrorResponse(
        String errorCode,      // stable, machine-readable
        String message,        // user-friendly
        String details,        // optional (for UI)
        Instant timestamp,
        String traceId         // for logs / support
) {}
