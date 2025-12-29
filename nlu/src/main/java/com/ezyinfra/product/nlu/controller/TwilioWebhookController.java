package com.ezyinfra.product.nlu.controller;

import com.ezyinfra.product.nlu.router.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class TwilioWebhookController {
    private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);
    private final WorkflowRouter workflowRouter;
    private final SessionManager sessionManager;

    public TwilioWebhookController(
            WorkflowRouter workflowRouter,
            SessionManager sessionManager) {
        this.workflowRouter = workflowRouter;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/whatsapp")
    public ResponseEntity<String> receiveWhatsApp(@RequestParam Map<String, String> allParams) {
        String phone = allParams.get("From");
        String body  = allParams.get("Body");
        UserSession session = sessionManager.getOrCreate(phone);
        if (!sessionManager.hasActiveWorkflow(phone)) {

            if (body.equalsIgnoreCase("1")) {
                session.setWorkflow(WorkflowType.GATEPASS);
                session.setState(WorkflowState.IN_PROGRESS);
                return ResponseEntity.ok("Please provide gatepass details");
            } else {
                return ResponseEntity.ok("Choose:\n1. Gatepass");
            }
        }
        // Twilio sends fields like From, To, Body, NumMedia, MediaUrl0, MediaContentType0, MessageSid, SmsStatus...
        String resp = workflowRouter.route(session, allParams);
        log.info("Router Response: {}",resp);
        return ResponseEntity.ok(resp);
    }

    private Map<String, String> buildRequestParams(HttpServletRequest request) {
        Map<String, String[]> rawParams = request.getParameterMap();

        Map<String, String> allParams = new HashMap<>();
        rawParams.forEach((k, v) -> {
            if (v != null && v.length > 0) {
                allParams.put(k, v[0]);
            }
        });
        return allParams;
    }

}