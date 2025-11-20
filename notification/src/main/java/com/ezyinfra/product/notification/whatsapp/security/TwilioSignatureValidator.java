package com.ezyinfra.product.notification.whatsapp.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TwilioSignatureValidator {
  private final String authToken;
  public TwilioSignatureValidator(@org.springframework.beans.factory.annotation.Value("${twilio.auth-token}") String authToken){
    this.authToken = authToken;
  }

  public boolean isValid(HttpServletRequest request, String xTwilioSignature) {
    if (xTwilioSignature == null) return false;
    String url = request.getRequestURL().toString();
    Map<String, String[]> params = request.getParameterMap();

    // Twilio expects sorted params
    List<String> keys = new ArrayList<>(params.keySet());
    Collections.sort(keys);

    StringBuilder sb = new StringBuilder(url);
    for (String key : keys) {
      String[] vals = params.get(key);
      // Twilio concatenates values without separators
      for (String value : vals) {
        sb.append(key).append(value);
      }
    }
    // calculate HMAC-SHA1 then base64
    byte[] hmacSha1 = new HmacUtils("HmacSHA1", authToken).hmac(sb.toString());
    String signature = Base64.encodeBase64String(hmacSha1);
    return signature.equals(xTwilioSignature);
  }
}