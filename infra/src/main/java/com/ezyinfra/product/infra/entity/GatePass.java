package com.ezyinfra.product.infra.entity;

import com.ezyinfra.product.common.enums.GatePassStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "gate_pass",
    indexes = {
        @Index(name = "idx_gatepass_person", columnList = "person_id"),
        @Index(name = "idx_gatepass_status", columnList = "status"),
        @Index(name = "idx_gatepass_request_time", columnList = "requestDateTime")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class GatePass extends AbstractPersistable{

    @ManyToOne(optional = false)
    private Person person;

    @Column(columnDefinition = "TEXT")
    private String purposeOfVisit;

    private String requestingCompany;

    private String requestingBranch;

    private String requestedCompany;

    private String requestedBranch;

    private String requestedBy;

    private String approver;

    private Instant requestDateTime;

    private Instant approvedDateTime;

    @Column(columnDefinition = "TEXT")
    private String approverRemarks;

    private Instant actualEntryTime;

    private Instant actualExitTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GatePassStatus status = GatePassStatus.REQUESTED;

    @OneToMany(mappedBy = "gatePass", cascade = CascadeType.ALL)
    private List<GatePassEquipment> equipments = new ArrayList<>();
}
