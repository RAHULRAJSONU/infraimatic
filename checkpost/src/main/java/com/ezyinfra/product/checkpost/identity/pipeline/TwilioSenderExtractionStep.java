package com.ezyinfra.product.checkpost.identity.pipeline;

import com.ezyinfra.product.checkpost.identity.util.CachedBodyHttpServletRequest;
import com.ezyinfra.product.common.dto.PhoneNumberParts;
import com.ezyinfra.product.common.utility.PhoneNumberParser;
import com.ezyinfra.product.common.utility.UtilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class TwilioSenderExtractionStep implements WebhookPipelineStep {

    @Override
    public boolean supports(WebhookType type) {
        return type == WebhookType.TWILIO_WHATSAPP;
    }

    @Override
    public void execute(WebhookContext ctx) {
        log.info("Executing TwilioSenderExtractionStep with WebhookContext: {}",ctx);
        if (!(ctx.getRequest() instanceof CachedBodyHttpServletRequest cached)) {
            throw new IllegalStateException("Cached request required for Twilio webhook");
        }

        Map<String, String> form =
                UtilityService.parseFormBody(cached.getCachedBody());

        String from = form.get("From");

        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException(
                    "Twilio webhook missing 'From' field (not a user message)"
            );
        }

        String mobile = from
                .replace("whatsapp:", "")
                .replace("+", "")
                .trim();
        PhoneNumberParts phoneNumber = PhoneNumberParser.parse(mobile, "IN");
        ctx.setSenderMobile(phoneNumber.nationalNumber());
    }
}

