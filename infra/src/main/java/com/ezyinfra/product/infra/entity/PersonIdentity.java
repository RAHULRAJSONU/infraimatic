package com.ezyinfra.product.infra.entity;

import com.ezyinfra.product.common.enums.IdentityType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(
    name = "person_identity",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_identity_type_number",
            columnNames = {"idType", "idNumber"}
        )
    },
    indexes = {
        @Index(name = "idx_identity_person", columnList = "person_id")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class PersonIdentity extends AbstractPersistable{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IdentityType idType;

    @Column(nullable = false, length = 100)
    private String idNumber;

    private LocalDate validTill;

    @ManyToOne
    private Attachment idCardAttachment;
}
