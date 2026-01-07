package com.ezyinfra.product.infraimatic.web;

import com.ezyinfra.product.infraimatic.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ProblemDetailsExceptionHandler {

    private final ProblemDetailFactory factory;

    public ProblemDetailsExceptionHandler(
            ProblemDetailFactory factory) {
        this.factory = factory;
    }

    // ---------------- Domain Exceptions ----------------

    @ExceptionHandler(ApprovalNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            ApprovalNotFoundException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.NOT_FOUND,
                ProblemTypes.APPROVAL_NOT_FOUND,
                "Approval not found",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ApprovalUnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(
            ApprovalUnauthorizedException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.FORBIDDEN,
                ProblemTypes.NOT_AUTHORIZED,
                "Not authorized",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ApprovalLockedException.class)
    public ResponseEntity<ProblemDetail> handleLocked(
            ApprovalLockedException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.CONFLICT,
                ProblemTypes.ENTITY_LOCKED,
                "Entity is locked",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ApprovalConcurrencyException.class)
    public ResponseEntity<ProblemDetail> handleConcurrency(
            ApprovalConcurrencyException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.CONFLICT,
                ProblemTypes.CONCURRENT_MODIFICATION,
                "Concurrent modification",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ApprovalInvalidStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidState(
            ApprovalInvalidStateException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                ProblemTypes.INVALID_STATE,
                "Invalid approval state",
                ex.getMessage(),
                request
        );
    }

    // ---------------- Validation ----------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail pd = factory.create(
                HttpStatus.BAD_REQUEST,
                ProblemTypes.VALIDATION_ERROR,
                "Validation failed",
                "Invalid request payload",
                request
        );

        pd.setProperty("errors", details);

        return ResponseEntity.badRequest().body(pd);
    }

    // ---------------- Fallback ----------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error", ex);

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ProblemTypes.INTERNAL_ERROR,
                "Internal server error",
                "Something went wrong. Contact support.",
                request
        );
    }

    // ---------------- Helper ----------------

    private ResponseEntity<ProblemDetail> build(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request) {

        ProblemDetail pd =
                factory.create(status, type, title, detail, request);

        return ResponseEntity.status(status).body(pd);
    }
}
