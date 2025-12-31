package com.ezyinfra.product.checkpost.identity.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioWebhookClassifierStep implements WebhookPipelineStep {

    @Override
    public boolean supports(WebhookType type) {
        return type == WebhookType.TWILIO_WHATSAPP;
    }

    @Override
    public void execute(WebhookContext ctx) {
        log.info("Executing TwilioSignatureStep with WebhookContext: {}",ctx);
    }
}
