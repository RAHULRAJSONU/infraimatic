package com.ezyinfra.product.messaging.model;

public final class ConsumerProperties {
    public int partitions = 1;                 // number of partitions for topic
    public int pollBatchSize = 10;
    public int workerThreads = 4;              // concurrent workers per instance
    public int maxAttempts = 5;
    public long baseBackoffMillis = 500L;      // exponential backoff base
    public long maxBackoffMillis = 30_000L;
    public long claimLockTtlMillis = 60_000L;  // advisory lock TTL (for rebalancing)
    public long idleDelayMillis = 200L;
}