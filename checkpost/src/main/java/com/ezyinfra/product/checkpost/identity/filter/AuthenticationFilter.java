package com.ezyinfra.product.checkpost.identity.filter;

import com.ezyinfra.product.checkpost.identity.data.repository.TokenRepository;
import com.ezyinfra.product.checkpost.identity.service.JwtService;
import com.ezyinfra.product.checkpost.identity.tenant.config.JwtTenantResolver;
import com.ezyinfra.product.checkpost.identity.tenant.config.TenantContext;
import com.ezyinfra.product.common.exception.ApiErrorResponse;
import com.ezyinfra.product.common.exception.AuthException;
import com.ezyinfra.product.common.utility.AppConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final JwtTenantResolver tenantResolver;
    private final SecurityContextRepository securityContextRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        return AppConstant.isPathExcluded(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = extractBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }

            final String tenantId = tenantResolver.resolveTenant(jwt)
                    .orElseThrow(() -> new AuthException("Tenant missing in token"));

            // ✅ Bind tenant WITHOUT ScopedValue boundary
            TenantContext.bind(tenantId);
            try {
                authenticate(jwt, request, response);
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }

        } catch (AccessDeniedException ade) {
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, ade.getMessage());
        } catch (AuthException ae) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
        } catch (Exception e) {
            log.warn("Authentication failed", e);
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        }
    }

    private void authenticate(String jwt, HttpServletRequest request, HttpServletResponse response) {

        final String userEmail = jwtService.extractUsername(jwt);
        if (userEmail == null) {
            throw new AuthException("Invalid JWT subject");
        }

        final String tokenType = jwtService.extractClaim(jwt,
                c -> (String) c.get(AppConstant.Jwt.TOKEN_TYPE));
        if (!AppConstant.Jwt.ACCESS_TOKEN.equals(tokenType)) {
            throw new AuthException("Invalid token type");
        }

        boolean tokenActive = tokenRepository.findByToken(jwt)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);

        if (!tokenActive) {
            throw new AuthException("Token revoked or expired");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        if (!jwtService.isTokenValid(jwt, userDetails)) {
            throw new AuthException("JWT validation failed");
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("User authenticated, UsernamePasswordAuthenticationToken: {}", auth);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }

    private static String extractBearerToken(String authHeader) {
        if (authHeader == null) return null;
        if (!authHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length()))
            return null;
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private void writeJsonError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new ApiErrorResponse(status, message)
                )
        );
    }
}
