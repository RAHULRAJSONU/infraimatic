package com.ezyinfra.product.notification.whatsapp.processor;

import com.ezyinfra.product.notification.whatsapp.repository.MessageRepository;
import com.ezyinfra.product.notification.whatsapp.service.MediaService;
import com.ezyinfra.product.notification.whatsapp.service.TwilioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class MessageProcessor{
  private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);
  private final MediaService mediaService;
  private final TwilioClient twilio;
  private final MessageRepository repo;
  private final WebClient backendClient;

  public MessageProcessor(MediaService mediaService,
                          TwilioClient twilio,
                          MessageRepository repo,
                          WebClient.Builder webClientBuilder) {
    this.mediaService = mediaService;
    this.twilio = twilio;
    this.repo = repo;
    this.backendClient = webClientBuilder.baseUrl("http://backend.internal/api").build();
  }

  public void process(Map<String,String> payload) {
    String messageSid = payload.getOrDefault("MessageSid", payload.get("SmsSid"));
    // fetch DB record, mark PROCESSING
    var entity = repo.findByProviderMessageId(messageSid).orElseThrow();

    try {
      int numMedia = Integer.parseInt(payload.getOrDefault("NumMedia", "0"));
      if (numMedia > 0) {
        // download all media: MediaUrl0..n
        for (int i=0;i<numMedia;i++) {
          String mediaUrl = payload.get("MediaUrl" + i);
          String contentType = payload.get("MediaContentType" + i);
          var uploaded = mediaService.downloadAndUpload(mediaUrl, contentType);
          uploaded.blockOptional().ifPresent(s -> entity.setMediaUrl(s.getUrl()));
        }
      }

      // optionally call backend to get response
      var resp = backendClient.post()
        .uri("/v1/chat/handle")
        .bodyValue(Map.of(
            "from", entity.getFromNumber(),
            "body", entity.getBody(),
            "mediaUrl", entity.getMediaUrl()
        ))
        .retrieve()
        .bodyToMono(Map.class)
        .block();

      // backend should return { replyText, sendMediaUrl? }
      String replyText = resp != null ? (String)resp.getOrDefault("replyText","Thanks!") : "Thanks!";
      // enqueue outbound via Twilio client
      twilio.sendWhatsApp(entity.getFromNumber(), replyText, null);

      entity.setStatus("RESPONDED");
      repo.save(entity);
    } catch (Exception ex) {
      log.error("processing failed for {}", messageSid, ex);
      entity.setStatus("FAILED");
      repo.save(entity);
      // optionally dead-letter or notify
    }
  }
}