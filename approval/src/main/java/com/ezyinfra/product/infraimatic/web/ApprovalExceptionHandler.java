package com.ezyinfra.product.infraimatic.web;

import com.ezyinfra.product.infraimatic.exception.ApprovalException;
import com.ezyinfra.product.infraimatic.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApprovalExceptionHandler {

    @ExceptionHandler(ApprovalException.class)
    public ResponseEntity<ErrorResponse> handleApprovalException(
            ApprovalException ex,
            HttpServletRequest request) {

        String traceId = getOrCreateTraceId(request);

        log.warn(
            "Approval error [{}] traceId={}",
            ex.getErrorCode(), traceId, ex
        );

        HttpStatus status = mapStatus(ex);

        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        null,
                        Instant.now(),
                        traceId
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String traceId = getOrCreateTraceId(request);

        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Invalid request",
                        details,
                        Instant.now(),
                        traceId
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

        String traceId = getOrCreateTraceId(request);

        log.error("Unexpected error traceId={}", traceId, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Something went wrong. Please contact support.",
                        null,
                        Instant.now(),
                        traceId
                ));
    }

    // ------------------------

    private HttpStatus mapStatus(ApprovalException ex) {
        return switch (ex.getErrorCode()) {
            case "APPROVAL_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "NOT_AUTHORIZED" -> HttpStatus.FORBIDDEN;
            case "ENTITY_LOCKED" -> HttpStatus.CONFLICT;
            case "CONCURRENT_MODIFICATION" -> HttpStatus.CONFLICT;
            case "INVALID_STATE" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private String getOrCreateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}
