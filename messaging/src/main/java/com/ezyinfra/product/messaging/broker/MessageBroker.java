package com.ezyinfra.product.messaging.broker;

import com.ezyinfra.product.messaging.model.ConsumerHandle;
import com.ezyinfra.product.messaging.model.ConsumerProperties;

import java.util.Map;

public interface MessageBroker {
    /**
     * Publish a message to a topic and partition. This should be called inside the same
     * transaction as your domain DB updates (if you want atomicity).
     * Returns the stored message id.
     */
    long publish(String topic, int partition, String key, Map<String, String> payload);

    /**
     * Start consuming: registers consumer group with the broker. This spawns background workers.
     * Returns a handle for lifecycle management.
     */
    ConsumerHandle startConsumer(String topic, String groupId, MessageProcessor processor, ConsumerProperties props);

    /**
     * Stop consumer by handle.
     */
    void stopConsumer(ConsumerHandle handle);
}
