package com.ezyinfra.product.notification.whatsapp.service;

import com.ezyinfra.product.messaging.broker.MessageBroker;
import com.ezyinfra.product.notification.whatsapp.model.MessageEntity;
import com.ezyinfra.product.notification.whatsapp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Replaces KafkaTemplate with MessageBroker publish.
 * Publishes the message inside the same DB transaction as saving the MessageEntity.
 */
@Service
public class IncomingMessageService {

    private final MessageBroker broker;
    private final MessageRepository messageRepo;
    // configurable number of partitions used by broker producers/consumers
    private final int partitions;

    public IncomingMessageService(MessageBroker broker,
                                  MessageRepository messageRepo,
                                  // default 8 partitions, can be overridden in application.yml
                                  @Value("${broker.partitions:8}") int partitions) {
        this.broker = broker;
        this.messageRepo = messageRepo;
        this.partitions = Math.max(1, partitions);
    }

    @Transactional
    public void handleIncoming(Map<String, String> params) {
        String messageSid = Optional.ofNullable(params.get("MessageSid"))
                .orElse(params.get("SmsSid"));
        if (messageSid == null) {
            messageSid = java.util.UUID.randomUUID().toString();
        }

        // idempotent - if message with same provider id exists, skip or update
        Optional<MessageEntity> existing = messageRepo.findByProviderMessageId(messageSid);
        if (existing.isPresent()) {
            // update status or ignore — keep behaviour same as before
            return;
        }

        MessageEntity m = new MessageEntity();
        m.setProviderMessageId(messageSid);
        m.setFromNumber(params.get("From"));
        m.setToNumber(params.get("To"));
        m.setBody(params.get("Body"));
        m.setStatus("RECEIVED");
        messageRepo.save(m);

        // determine partition deterministically from messageSid
        int partition = Math.floorMod(messageSid.hashCode(), partitions);

        // publish into DB-backed broker (atomic with messageRepo.save because same transaction)
        broker.publish("whatsapp.incoming", partition, messageSid, params);
    }
}