package com.ezyinfra.product.infra.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(
    name = "gate_pass_equipment",
    indexes = @Index(name = "idx_equipment_gatepass", columnList = "gate_pass_id")
)
@Data
@EqualsAndHashCode(callSuper = true)
public class GatePassEquipment extends AbstractPersistable{

    @ManyToOne(optional = false)
    private GatePass gatePass;

    private String equipmentName;
    private Integer quantity;
}
