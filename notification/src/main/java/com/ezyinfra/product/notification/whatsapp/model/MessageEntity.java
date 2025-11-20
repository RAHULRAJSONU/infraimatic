package com.ezyinfra.product.notification.whatsapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_provider_message_id", columnList = "providerMessageId", unique = true)
})
public class MessageEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable=false)
    private String providerMessageId; // Twilio's MessageSid or webhook unique id

    @Column(nullable=false)
    private String fromNumber;

    @Column(nullable=false)
    private String toNumber;

    @Column(length = 10000)
    private String body;

    @Column
    private String mediaUrl; // S3 location

    @Column(nullable=false)
    private String status; // RECEIVED, PROCESSING, RESPONDED, FAILED

    @Column(nullable=false)
    private Instant createdAt = Instant.now();

}