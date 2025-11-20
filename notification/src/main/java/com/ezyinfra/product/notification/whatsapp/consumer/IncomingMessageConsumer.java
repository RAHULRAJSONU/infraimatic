package com.ezyinfra.product.notification.whatsapp.consumer;

import com.ezyinfra.product.messaging.broker.MessageBroker;
import com.ezyinfra.product.messaging.model.ConsumerHandle;
import com.ezyinfra.product.messaging.model.ConsumerProperties;
import com.ezyinfra.product.notification.whatsapp.processor.MessageProcessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers a consumer with the MessageBroker and delegates to MessageProcessor.
 * Broker handles retries, DLQ, partitioning, concurrency etc.
 */
@Component
public class IncomingMessageConsumer {

    private final MessageBroker broker;
    private final MessageProcessor processor;
    private final String topic;
    private final String groupId;
    private final int partitions;
    private final int workerThreads;
    private final int batchSize;
    private final int maxAttempts;

    private ConsumerHandle handle;

    public IncomingMessageConsumer(MessageBroker broker,
                                   MessageProcessor processor,
                                   @Value("${kafka.topics.incoming:whatsapp.incoming}") String topic,
                                   @Value("${broker.consumer.group:whatsapp-processor}") String groupId,
                                   @Value("${broker.partitions:8}") int partitions,
                                   @Value("${broker.consumer.workers:4}") int workerThreads,
                                   @Value("${broker.consumer.batch-size:20}") int batchSize,
                                   @Value("${broker.consumer.max-attempts:5}") int maxAttempts) {
        this.broker = broker;
        this.processor = processor;
        this.topic = topic;
        this.groupId = groupId;
        this.partitions = Math.max(1, partitions);
        this.workerThreads = Math.max(1, workerThreads);
        this.batchSize = Math.max(1, batchSize);
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    @PostConstruct
    public void start() {
        ConsumerProperties props = new ConsumerProperties();
        props.partitions = partitions;
        props.workerThreads = workerThreads;
        props.pollBatchSize = batchSize;
        props.maxAttempts = maxAttempts;
        // optional tuning (backoff, idle, etc.) can be set here or exposed via properties
        handle = broker.startConsumer(topic, groupId, payload -> {
            // Delegate to existing processor; exceptions are expected to bubble up so broker can retry/DLQ
            processor.process(payload);
        }, props);
    }

    @PreDestroy
    public void stop() {
        if (handle != null) {
            broker.stopConsumer(handle);
        }
    }
}