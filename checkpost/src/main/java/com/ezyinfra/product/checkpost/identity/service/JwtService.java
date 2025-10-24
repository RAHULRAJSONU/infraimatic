package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;


public interface JwtService {

    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(User userDetails);

    String generateToken(Map<String, Object> extraClaims, User userDetails);

    String generateRefreshToken(Map<String, Object> extraClaim, User userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    Claims extractAllClaims(String token);

}