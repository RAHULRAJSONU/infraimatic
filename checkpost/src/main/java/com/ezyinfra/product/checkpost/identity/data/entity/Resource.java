package com.ezyinfra.product.checkpost.identity.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Builder
@Table(name = "identity_resource")
@NoArgsConstructor
@AllArgsConstructor
public class Resource extends AbstractPersistable {

    @Column(nullable = false, length = 56, unique = true)
    private String name;
    private String description;
    private boolean active;

}