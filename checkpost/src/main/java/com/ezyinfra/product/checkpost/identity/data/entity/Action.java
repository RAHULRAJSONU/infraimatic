package com.ezyinfra.product.checkpost.identity.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@Table(name = "identity_action")
@NoArgsConstructor
@AllArgsConstructor
public class Action extends AbstractPersistable {
    @Column(nullable = false, length = 56, unique = true)
    private String name;
    @Column(nullable = false, length = 56)
    private String description;
    private boolean active;

    @Override
    public String toString() {
        return name;
    }
}