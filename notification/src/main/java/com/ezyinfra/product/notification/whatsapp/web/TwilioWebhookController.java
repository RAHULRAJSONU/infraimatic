package com.ezyinfra.product.notification.whatsapp.web;

import com.ezyinfra.product.notification.whatsapp.security.TwilioSignatureValidator;
import com.ezyinfra.product.notification.whatsapp.service.IncomingMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/webhook/twilio")
public class TwilioWebhookController {
  private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);
  private final TwilioSignatureValidator validator;
  private final IncomingMessageService incomingService;

  @Value("${twilio.webhook.enable-signature-validation:true}")
  private boolean validationEnabled;

  public TwilioWebhookController(TwilioSignatureValidator validator, IncomingMessageService incomingService) {
    this.validator = validator;
    this.incomingService = incomingService;
  }

  @PostMapping("/whatsapp")
  public ResponseEntity<String> receiveWhatsApp(HttpServletRequest request, @RequestParam Map<String,String> allParams, @RequestHeader(value="X-Twilio-Signature", required=false) String signature) {
    if (validationEnabled && !validator.isValid(request, signature)) {
      log.warn("Invalid Twilio signature");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
    }

    // Twilio sends fields like From, To, Body, NumMedia, MediaUrl0, MediaContentType0, MessageSid, SmsStatus...
    incomingService.handleIncoming(allParams);
    // reply 200 quickly so Twilio doesn't retry. Actual replies are sent via Twilio REST API async.
    return ResponseEntity.ok("received");
  }
}