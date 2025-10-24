package com.ezyinfra.product.checkpost.identity.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ezyinfra.product.common.exception.ApiErrorResponse;
import com.ezyinfra.product.common.exception.AuthException;
import com.ezyinfra.product.common.utility.AppConstant;
import com.ezyinfra.product.checkpost.identity.data.repository.TokenRepository;
import com.ezyinfra.product.checkpost.identity.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

@Slf4j
//@Component
@RequiredArgsConstructor
public class AuthenticationFilterV0 extends OncePerRequestFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final BeanFactory beanFactory;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    private static final List<String> EXCLUDED = List.of(
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/webjars/**",
            "/api/v1/tenant/register",
            "/api/v1/user/**",
            "/api/v1/identity/authn/**",
            "/api/v1/oauth/**",
            "/v2/api-docs"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return EXCLUDED.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {

//            if (AppConstant.isPathExcluded(request.getServletPath())) {
//                filterChain.doFilter(request, response);
//                return;
//            }
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                System.out.println("Header: " + name + " -> " + request.getHeader(name));
            }
            var apiKey = request.getHeader("X-API-KEY");
            var apiSecret = request.getHeader("X-SECRET-KEY");

            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            final String jwt;
            final String userEmail;
            log.info("X-API-KEY: {}, X-SECRET-KEY: {}", apiKey, apiSecret);


            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            jwt = authHeader.substring(7);

            userEmail = jwtService.extractUsername(jwt);
            String tokenType = jwtService.extractClaim(jwt, c -> (String) c.get(AppConstant.Jwt.TOKEN_TYPE));
            if (tokenType == null) {
                throw new AuthException("Invalid token.");
            }
            List<String> authorities = (List<String>) jwtService.extractAllClaims(jwt).get("authorities");
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);
                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (AccessDeniedException e) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(toJson(errorResponse));
        } catch (AuthException ae) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(toJson(errorResponse));
        }
    }

    private String toJson(ApiErrorResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "";
        }
    }

}