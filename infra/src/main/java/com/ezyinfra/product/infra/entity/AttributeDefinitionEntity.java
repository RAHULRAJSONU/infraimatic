package com.ezyinfra.product.infra.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "attribute_definitions",
        indexes = {
                @Index(name = "idx_attrdef_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_attrdef_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                // Ensure (tenant_id, attribute_ref) is unique; allows same ref across tenants and a global version.
                @UniqueConstraint(name = "uk_attrdef_tenant_ref", columnNames = {"tenant_id", "attribute_ref"})
        }
)
@EqualsAndHashCode(callSuper = true)
public class AttributeDefinitionEntity extends AbstractPersistable {

    @Column(name = "attribute_ref", nullable = false, length = 255)
    private String attributeRef;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_schema", columnDefinition = "jsonb")
    private JsonNode jsonSchema;
}
