package com.ezyinfra.product.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "attachment")
@Data
@EqualsAndHashCode(callSuper = true)
public class Attachment  extends AbstractPersistable{

    private String fileName;
    private String contentType;

    @Column(nullable = false)
    private String storageUrl;
}
