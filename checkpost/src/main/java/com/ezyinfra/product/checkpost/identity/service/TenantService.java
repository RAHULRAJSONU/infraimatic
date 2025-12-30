package com.ezyinfra.product.checkpost.identity.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantService {

    private final JdbcTemplate jdbcTemplate;

    public TenantService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> resolveTenantByMobile(String mobile) {
        String sql = """
            SELECT tenant_id
            FROM identity_user
            WHERE phone_number = ?
              AND status = 'ACTIVE'
        """;
        return jdbcTemplate.query(
                sql,
                ps -> ps.setString(1, mobile),
                rs -> rs.next() ? Optional.of(rs.getString("tenant_id")) : Optional.empty()
        );
    }

    public Optional<String> resolveTenantByEmail(String email) {
        String sql = """
            SELECT tenant_id
            FROM identity_user
            WHERE email = ?
              AND status = 'ACTIVE'
        """;
        return jdbcTemplate.query(
                sql,
                ps -> ps.setString(1, email),
                rs -> rs.next() ? Optional.of(rs.getString("tenant_id")) : Optional.empty()
        );
    }
}
