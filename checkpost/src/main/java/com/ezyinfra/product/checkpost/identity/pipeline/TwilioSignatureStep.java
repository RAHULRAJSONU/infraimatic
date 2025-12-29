package com.ezyinfra.product.checkpost.identity.pipeline;

import com.ezyinfra.product.checkpost.identity.util.CachedBodyHttpServletRequest;
import com.ezyinfra.product.common.utility.UtilityService;
import com.twilio.security.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class TwilioSignatureStep implements WebhookPipelineStep {

    private final RequestValidator validator;

    public TwilioSignatureStep(@Value("${twilio.auth-token}") String authToken) {
        this.validator = new RequestValidator(authToken);
    }

    @Override
    public boolean supports(WebhookType type) {
        return type == WebhookType.TWILIO_WHATSAPP;
    }

    @Override
    public void execute(WebhookContext ctx) {
        log.info("Executing TwilioSignatureStep with WebhookContext: {}",ctx);
        HttpServletRequest req = ctx.getRequest();

        if (!(req instanceof CachedBodyHttpServletRequest cached)) {
            throw new IllegalStateException("Cached request required for Twilio");
        }

        String signature = req.getHeader("X-Twilio-Signature");
        if (signature == null) {
            throw new SecurityException("Missing Twilio signature");
        }

        String url = buildPublicUrl(req);
        log.info("Twilio URL used for signature: {}", url);
        Map<String, String> params = UtilityService.parseFormBody(cached.getCachedBody());

        if (!validator.validate(url, params, signature)) {
            throw new SecurityException("Invalid Twilio signature");
        }
    }

    private String buildPublicUrl(HttpServletRequest request) {

        String scheme = headerOrDefault(request, "X-Forwarded-Proto", request.getScheme());
        String host   = headerOrDefault(request, "X-Forwarded-Host", request.getServerName());
        String port   = request.getHeader("X-Forwarded-Port");

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(host);

        // append port only if non-standard
        if (port != null &&
                !("80".equals(port) || "443".equals(port))) {
            url.append(":").append(port);
        }

        url.append(request.getRequestURI());
        return url.toString();
    }

    private String headerOrDefault(
            HttpServletRequest request,
            String header,
            String defaultValue) {

        String value = request.getHeader(header);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
