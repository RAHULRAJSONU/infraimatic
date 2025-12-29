package com.ezyinfra.product.notification.whatsapp.security;

import com.twilio.security.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TwilioSignatureValidator {

    private final RequestValidator requestValidator;

    public TwilioSignatureValidator(@Value("${twilio.auth-token}") String authToken) {
        this.requestValidator = new RequestValidator(authToken);
    }

    public boolean isValid(HttpServletRequest request, String xTwilioSignature) {
        if (xTwilioSignature == null || xTwilioSignature.isBlank()) {
            return false;
        }

        String url = buildTwilioUrl(request);

        // Convert parameterMap<String, String[]> → Map<String, String>
        // (for Twilio we only care about POST form params, which is what getParameterMap() returns for x-www-form-urlencoded)
        Map<String, String[]> raw = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> e : raw.entrySet()) {
            String[] vals = e.getValue();
            if (vals != null && vals.length > 0) {
                params.put(e.getKey(), vals[0]); // Twilio uses first value if multiple
            }
        }

        return requestValidator.validate(url, params, xTwilioSignature.trim());
    }

    private String buildTwilioUrl(HttpServletRequest request) {
        String scheme = headerOrDefault(request, "X-Forwarded-Proto", request.getScheme());
        String host   = headerOrDefault(request, "X-Forwarded-Host", request.getServerName());

        String forwardedPort = request.getHeader("X-Forwarded-Port");
        int port = forwardedPort != null ? Integer.parseInt(forwardedPort) : request.getServerPort();

        boolean standardPort = (scheme.equalsIgnoreCase("http") && port == 80)
                || (scheme.equalsIgnoreCase("https") && port == 443);

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(host);
        if (!standardPort && host.indexOf(':') == -1) {
            url.append(':').append(port);
        }
        url.append(request.getRequestURI());

        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            url.append('?').append(queryString);
        }

        return url.toString();
    }

    private String headerOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getHeader(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
