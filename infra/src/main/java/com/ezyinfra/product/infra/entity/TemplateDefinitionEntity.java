package com.ezyinfra.product.infra.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
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
        name = "template_definitions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_template_tenant_type_version",
                columnNames = {"tenant_id", "type", "version"}
        ),
        indexes = {
                @Index(name = "idx_tpl_tenant_type", columnList = "tenant_id,type"),
                @Index(name = "idx_tpl_created_at", columnList = "created_at")
        }
)
public class TemplateDefinitionEntity  extends AbstractPersistable {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", length = 255)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_schema", columnDefinition = "jsonb")
    private JsonNode jsonSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attribute_refs", columnDefinition = "jsonb")
    private JsonNode attributeRefs;
}
