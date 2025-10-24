package com.ezyinfra.product.checkpost.identity.data.repository;

import com.ezyinfra.product.checkpost.identity.data.entity.Token;
import com.ezyinfra.product.checkpost.identity.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query(value = """
            select t from Token t inner join User u\s
            on t.user.id = u.id\s
            where u.id = :id and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUser(UUID id);

    @Query("""
            SELECT t FROM Token t WHERE t.user = :user AND (t.expired = false or t.revoked = false)
            """)
    List<Token> findValidTokenByUser(@Param("user") User user);

    Optional<Token> findByToken(String token);

    Optional<Token> findByRefreshToken(String refreshToken);

    @Modifying
    @Query(value = """
            update Token t set t.expired = true, t.revoked = true
            where t.token = :token
            """)
    @Transactional
    void revoke(@Param(value = "token") String token);

    @Modifying
    @Query(value = """
            update Token t set t.expired = true, t.revoked = true
            where t.user = :user and t.expired = false and t.revoked = false
            """)
    @Transactional
    void revokeAll(@Param(value = "user") User user);
}