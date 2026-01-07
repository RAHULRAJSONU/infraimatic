package com.ezyinfra.product.infraimatic.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

@Component
public class ProblemDetailFactory {

    public ProblemDetail create(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setType(URI.create(type));
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty(
                "traceId",
                getOrCreateTraceId(request)
        );

        return problem;
    }

    private String getOrCreateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        return traceId != null
                ? traceId
                : UUID.randomUUID().toString();
    }
}
