package com.ezyinfra.product.notification.whatsapp.service;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

@Service
public class TwilioClient {
  private final WebClient webClient;
  private final String accountSid;
  private final String from;

  public TwilioClient(WebClient.Builder webClientBuilder,
                      @Value("${twilio.account-sid}") String accountSid,
                      @Value("${twilio.auth-token}") String authToken,
                      @Value("${twilio.whatsapp-from}") String from) {
    this.accountSid = accountSid;
    this.from = from;
    this.webClient = webClientBuilder.baseUrl("https://api.twilio.com/2010-04-01")
      .defaultHeaders(headers -> headers.setBasicAuth(accountSid, authToken))
      .build();
  }

  public Mono<Map> sendWhatsApp(String to, String body, String mediaUrl) {
    var form = new LinkedMultiValueMap<String, String>();
    form.add("From", from); // e.g. whatsapp:+1415...
    form.add("To", to);
    if (body != null) form.add("Body", body);
    if (mediaUrl != null) form.add("MediaUrl", mediaUrl);

    return webClient.post()
            .uri("/Accounts/{AccountSid}/Messages.json", accountSid)
            .bodyValue(form)
            .retrieve()
            .bodyToMono(Map.class);
  }
}