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
    private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);
    private final WorkflowRouter workflowRouter;
    private final SessionManager sessionManager;
    private final MediaService mediaService;
    private final AudioTranscriptionService transcriptionService;
    private final WhatsAppPipeline pipeline;

    public TwilioWebhookController(
            WorkflowRouter workflowRouter,
            SessionManager sessionManager,
            MediaService mediaService,
            AudioTranscriptionService transcriptionService,
            WhatsAppPipeline pipeline) {
        this.workflowRouter = workflowRouter;
        this.sessionManager = sessionManager;
        this.mediaService = mediaService;
        this.transcriptionService = transcriptionService;
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

//    private Map<String, String> buildRequestParams(HttpServletRequest request) {
//        Map<String, String[]> rawParams = request.getParameterMap();
//
//        Map<String, String> allParams = new HashMap<>();
//        rawParams.forEach((k, v) -> {
//            if (v != null && v.length > 0) {
//                allParams.put(k, v[0]);
//            }
//        });
//        return allParams;
//    }

//    private boolean isMediaMessage(Map<String, String> params) {
//        try {
//            return Integer.parseInt(params.getOrDefault("NumMedia", "0")) > 0;
//        } catch (NumberFormatException e) {
//            return false;
//        }
//    }

//    private String extractTextOrAudio(Map<String, String> params) {
//
//        String body = params.getOrDefault("Body", "").trim();
//        if (!body.isBlank()) {
//            return body;
//        }
//
//        int numMedia = Integer.parseInt(params.getOrDefault("NumMedia", "0"));
//        if (numMedia == 0) {
//            return "";
//        }
//
//        String mediaUrl = params.get("MediaUrl0");
//        String contentType = params.get("MediaContentType0");
//
//        if (mediaUrl == null || !contentType.startsWith("audio")) {
//            return "";
//        }
//
//        Mono<StorageResult> resultMono = mediaService.downloadAndUpload(mediaUrl, contentType);
//
//        return transcriptionService
//                .transcribe(resultMono, contentType)
//                .map(this::normalizeTranscript)
//                .orElse("");
//    }

//    private String normalizeTranscript(String text) {
//        return text
//                .toLowerCase()
//                .replaceAll("\\buh+\\b|\\bum+\\b|\\ber+\\b", "")
//                .replaceAll("[^a-z0-9 ]", "")
//                .trim();
//    }


}