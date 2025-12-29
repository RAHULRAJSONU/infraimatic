package com.ezyinfra.product.infra.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "person",
    indexes = {
        @Index(name = "idx_person_phone", columnList = "phoneNo")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class Person extends AbstractPersistable{

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(length = 200)
    private String fatherName;

    @Column(nullable = false, length = 20)
    private String phoneNo;

    @Column(length = 20)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonIdentity> identities = new ArrayList<>();
}
