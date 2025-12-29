//package com.ezyinfra.product.nlu.config;
//
//import com.ezyinfra.product.checkpost.identity.util.CachedBodyHttpServletRequest;
//import com.ezyinfra.product.common.utility.UtilityService;
//import com.ezyinfra.product.notification.whatsapp.security.TwilioSignatureValidator;
//import com.twilio.security.RequestValidator;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//import java.util.TreeMap;
//
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class TwilioSignatureFilter extends OncePerRequestFilter {
//
//    private final String authToken;
//
//    public TwilioSignatureFilter(@Value("${twilio.auth-token}") String authToken) {
//        this.authToken = authToken;
//    }
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain)
//            throws ServletException, IOException {
//
//        if (!isTwilioWebhook(request)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        boolean valid = validateTwilioSignature(request, authToken);
//
//        if (!valid) {
//            response.setStatus(HttpStatus.FORBIDDEN.value());
//            response.getWriter().write("Invalid signature");
//            return;
//        }
//        filterChain.doFilter(request, response);
//    }
//
//    private boolean isTwilioWebhook(HttpServletRequest request) {
//        return request.getRequestURI().startsWith("/webhooks/whatsapp");
//    }
//
//    private boolean validateTwilioSignature(HttpServletRequest request, String authToken) {
//
//        String signature = request.getHeader("X-Twilio-Signature");
//        if (signature == null) return false;
//
//        String url = request.getRequestURL().toString();
//
//        Map<String, String> params = new TreeMap<>();
//
//        if (request instanceof CachedBodyHttpServletRequest cached) {
//            String body = new String(
//                    cached.getCachedBody(),
//                    StandardCharsets.UTF_8
//            );
//            params.putAll(UtilityService.parseFormBody(body));
//        }
//
//        RequestValidator validator =
//                new RequestValidator(authToken);
//
//        return validator.validate(url, params, signature);
//    }
//}
