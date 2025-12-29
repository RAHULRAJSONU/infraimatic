package com.ezyinfra.product.checkpost.identity.data.entity;

import com.ezyinfra.product.common.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "identity_token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "token", columnDefinition = "TEXT", nullable = false, unique = true)
    private String token;

    @Column(name = "refreshToken", columnDefinition = "TEXT", nullable = false, unique = true)
    private String refreshToken;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TokenType tokenType = TokenType.BEARER;

    private boolean revoked;

    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}