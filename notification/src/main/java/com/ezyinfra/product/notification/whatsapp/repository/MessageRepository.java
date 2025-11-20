package com.ezyinfra.product.notification.whatsapp.repository;

import com.ezyinfra.product.notification.whatsapp.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    Optional<MessageEntity> findByProviderMessageId(String providerMessageId);
}
