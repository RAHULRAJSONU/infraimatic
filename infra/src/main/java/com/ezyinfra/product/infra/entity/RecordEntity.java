package com.ezyinfra.product.infra.entity;

import com.ezyinfra.product.domain.EntryStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "records",
        indexes = {
                @Index(name = "idx_rec_tenant_status", columnList = "tenant_id,status"),
                @Index(name = "idx_rec_tenant_type", columnList = "tenant_id,type"),
                @Index(name = "idx_rec_created_at", columnList = "created_at"),
                @Index(name = "idx_rec_client_submission_id", columnList = "tenant_id,client_submission_id")
        }
)
public class RecordEntity  extends AbstractPersistable {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "client_submission_id", length = 255)
    private String clientSubmissionId;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "fk_record_template"))
    private TemplateDefinitionEntity template;

    @Column(name = "template_version", updatable = false)
    private Integer templateVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EntryStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private JsonNode payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "normalized", columnDefinition = "jsonb")
    private JsonNode normalized;

}
