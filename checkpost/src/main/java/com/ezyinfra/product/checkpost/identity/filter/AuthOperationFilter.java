//package com.ezyinfra.product.checkpost.identity.filter;
//
//import com.ezyinfra.product.checkpost.identity.data.repository.TokenRepository;
//import com.ezyinfra.product.checkpost.identity.service.JwtService;
//import com.ezyinfra.product.checkpost.identity.tenant.config.JwtTenantResolver;
//import com.ezyinfra.product.checkpost.identity.tenant.config.TenantContext;
//import com.ezyinfra.product.common.exception.ApiErrorResponse;
//import com.ezyinfra.product.common.exception.AuthException;
//import com.ezyinfra.product.common.utility.AppConstant;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.Objects;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class AuthOperationFilter extends OncePerRequestFilter {
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private static final String BEARER_PREFIX = "Bearer ";
//    private final JwtService jwtService;
//    private final UserDetailsService userDetailsService;
//    private final TokenRepository tokenRepository;
//    private final JwtTenantResolver tenantResolver;
//
//    private static final List<String> EXCLUDED = List.of(
//            "/api/v1/tenant/register",
//            "/api/v1/identity/authn/register"
//    );
//
//    private final AntPathMatcher pathMatcher = new AntPathMatcher();
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
//        final String path = request.getServletPath();
//        return EXCLUDED.stream().noneMatch(p -> pathMatcher.match(p, path));
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//        try {
//            // If already authenticated (e.g., downstream filter set it), continue
//            if (SecurityContextHolder.getContext().getAuthentication() != null) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // Parse bearer token
//            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//            final String jwt = extractBearerToken(authHeader);
//
//            // If no Bearer, treat as anonymous and continue
//            if (jwt == null) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            final String tenantId = tenantResolver.resolveTenant(jwt)
//                    .orElseThrow(() -> new AuthException("Tenant missing in token"));
//
//            // 🔐 Execute authentication WITH tenant bound
//            TenantContext.executeInTenantContext(tenantId, () -> {
//                try {
//                    authenticateAndContinue(jwt, request, response, filterChain);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//        } catch (AccessDeniedException ade) {
//            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, ade.getMessage());
//        } catch (AuthException ae) {
//            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
//        } catch (Exception e) {
//            log.warn("Authentication processing failed: {}", e.getMessage(), e);
//            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed.");
//        }
//    }
//
//    /**
//     * Authentication logic isolated to keep tenant binding clean
//     */
//    private void authenticateAndContinue(
//            String jwt,
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain)
//            throws IOException, ServletException {
//
//        // Validate & build authentication
//        final String userEmail = jwtService.extractUsername(jwt);
//        if (userEmail == null || userEmail.isBlank()) {
//            throw new AuthException("Invalid token: subject missing.");
//        }
//
//        // Optionally enforce token type
//        String tokenType = jwtService.extractClaim(jwt, claims -> (String) claims.get(AppConstant.Jwt.TOKEN_TYPE));
//        if (tokenType == null) {
//            throw new AuthException("Invalid token: token_type missing.");
//        }
//
//        // Repo-backed token validity (revocation/expiration flags)
//        boolean isTokenActive = tokenRepository.findByToken(jwt)
//                .map(t -> !t.isExpired() && !t.isRevoked())
//                .orElse(false);
//        if (!isTokenActive) {
//            throw new AuthException("Token is expired or revoked.");
//        }
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
//        if (!jwtService.isTokenValid(jwt, userDetails)) {
//            throw new AuthException("Token signature or claims invalid.");
//        }
//
//        // Build authentication and populate context
//        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                userDetails,
//                null,
//                userDetails.getAuthorities()
//        );
//        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(request, response);
//    }
//
//    private static String extractBearerToken(String authHeader) {
//        if (authHeader == null) return null;
//        if (!authHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) return null;
//        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
//        return token.isEmpty() ? null : token;
//    }
//
//    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
//        // Don’t continue the chain after writing the error
//        response.setStatus(status);
//        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//        response.setContentType("application/json");
//        ApiErrorResponse body = new ApiErrorResponse(status, Objects.toString(message, ""));
//        response.getWriter().write(toJson(body));
//        response.getWriter().flush();
//    }
//
//    private String toJson(ApiErrorResponse body) {
//        try {
//            return objectMapper.writeValueAsString(body);
//        } catch (Exception e) {
//            // extremely unlikely; fall back to a tiny JSON
//            return "{\"status\":" + body.errorCode() + ",\"message\":\"" + safe(body.description()) + "\"}";
//        }
//    }
//
//    private static String safe(String s) {
//        return s == null ? "" : s.replace("\"", "\\\"");
//    }
//}
