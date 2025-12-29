package com.ezyinfra.product.infra.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractPersistable implements Persistable<UUID>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private boolean deleted;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "ui_hint", length = 100)
    private String uiHint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nlp_aliases", columnDefinition = "jsonb")
    private JsonNode nlpAliases;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "deleted_on")
    private LocalDateTime deletedOn;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    public boolean isNew() {
        return this.id == null;
    }

    @PreUpdate
    @PrePersist
    public void beforeAnyUpdate() {
        if (deleted) {

//            if (deletedBy == null) {
//                Optional<User> loggedInUser = IdentityService.getLoggedInUser();
//                deletedBy = loggedInUser.map(User::getUsername).get();
//            }

            if (getDeletedOn() == null) {
                deletedOn = LocalDateTime.now();
            }
        }
    }
}
