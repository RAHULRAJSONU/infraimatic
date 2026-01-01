package com.ezyinfra.product.nlu.controller;

import com.ezyinfra.product.nlu.pipeline.PipelineContext;
import com.ezyinfra.product.nlu.pipeline.whatsapp.WhatsAppPipeline;
import com.ezyinfra.product.nlu.service.AudioTranscriptionService;
import com.ezyinfra.product.nlu.workflow.router.SessionManager;
import com.ezyinfra.product.nlu.workflow.router.UserSession;
import com.ezyinfra.product.nlu.workflow.router.WorkflowRouter;
import com.ezyinfra.product.notification.whatsapp.service.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
public class TwilioWebhookController {
    private final SessionManager sessionManager;
    private final WhatsAppPipeline pipeline;

    public TwilioWebhookController(
            SessionManager sessionManager,
            WhatsAppPipeline pipeline) {
        this.sessionManager = sessionManager;
        this.pipeline = pipeline;
    }

    @PostMapping("/whatsapp")
    public Mono<ResponseEntity<String>> receiveWhatsApp(
            @RequestParam Map<String, String> params
    ) {
        String phone = params.get("From");
        UserSession session = sessionManager.getOrCreate(phone);

        PipelineContext ctx = PipelineContext.builder()
                .requestId(UUID.randomUUID().toString())
                .phone(phone)
                .session(session)
                .event(params)
                .build();

        return pipeline.execute(ctx)
                .map(ResponseEntity::ok);
    }

}