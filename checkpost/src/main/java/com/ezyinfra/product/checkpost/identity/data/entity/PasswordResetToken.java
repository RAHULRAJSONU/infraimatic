package com.ezyinfra.product.checkpost.identity.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "identity_reset_password_token")
public class PasswordResetToken extends AbstractPersistable {

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private String oldPasswordHash;

    private Date expiryDate;
}
