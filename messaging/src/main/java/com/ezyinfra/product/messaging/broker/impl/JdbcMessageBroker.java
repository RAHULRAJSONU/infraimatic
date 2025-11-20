package com.ezyinfra.product.messaging.broker.impl;

import com.ezyinfra.product.messaging.broker.MessageBroker;
import com.ezyinfra.product.messaging.broker.MessageProcessor;
import com.ezyinfra.product.messaging.model.ConsumerHandle;
import com.ezyinfra.product.messaging.model.ConsumerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToDoubleFunction;

/**
 * Postgres-backed message broker.
 *
 * Durable publish + partitioned consumers that claim partitions using advisory locks.
 */
@Component
public class JdbcMessageBroker implements MessageBroker {

    private static final Logger log = LoggerFactory.getLogger(JdbcMessageBroker.class);

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final ObjectMapper mapper;
    private final TransactionTemplate tx;
    private final MeterRegistry meter;

    private final ScheduledExecutorService scheduler;
    private final ExecutorService workerPool;
    private final Map<String, ConsumerRuntime> runtimes = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Timer publishTimer;

    public JdbcMessageBroker(JdbcTemplate jdbc, DataSource dataSource, ObjectMapper mapper,
                             PlatformTransactionManager txManager, MeterRegistry meter) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.mapper = mapper;
        this.tx = new TransactionTemplate(txManager);
        this.meter = meter;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "broker-scheduler-" + UUID.randomUUID());
            t.setDaemon(true);
            return t;
        });
        this.workerPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "broker-worker-" + UUID.randomUUID());
            t.setDaemon(true);
            return t;
        });
        this.publishTimer = meter.timer("broker.publish.duration");
    }

    // -------------------- Publish --------------------

    @Override
    public long publish(String topic, int partition, String key, Map<String, String> payload) {
        long start = System.nanoTime();
        try {
            final String sql = "INSERT INTO broker_messages(topic, partition, key, payload, status, available_at) " +
                    "VALUES (?, ?, ?, cast(? as jsonb), 'READY', now())";
            KeyHolder kh = new GeneratedKeyHolder();
            final String json;
            try {
                json = mapper.writeValueAsString(payload);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize payload", e);
            }

            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, topic);
                ps.setInt(2, partition);
                ps.setString(3, key);
                ps.setString(4, json);
                return ps;
            }, kh);

            Number id = Objects.requireNonNull(kh.getKey());
            long created = id.longValue();
            meter.counter("broker.publish.count", "topic", topic).increment();
            return created;
        } finally {
            publishTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    // -------------------- Consumer lifecycle --------------------

    @Override
    public ConsumerHandle startConsumer(String topic, String groupId, MessageProcessor processor, ConsumerProperties props) {
        String id = topic + "|" + groupId;
        if (runtimes.containsKey(id)) {
            throw new IllegalStateException("Consumer already running: " + id);
        }
        ConsumerRuntime runtime = new ConsumerRuntime(topic, groupId, processor, props);
        runtimes.put(id, runtime);
        runtime.start();
        // Use Tags.of + state-object overload so we can read runtime size at scrape time
        ToDoubleFunction<Map<String, ConsumerRuntime>> fn = m -> (double) m.size();
        meter.gauge("broker.consumer.count", Tags.of("group", groupId), runtimes, fn);
        return new ConsumerHandle(id);
    }

    @Override
    public void stopConsumer(ConsumerHandle handle) {
        ConsumerRuntime runtime = runtimes.remove(handle.getId());
        if (runtime != null) runtime.stop();
    }

    @PreDestroy
    public void shutdown() {
        if (closed.compareAndSet(false, true)) {
            runtimes.values().forEach(ConsumerRuntime::stop);
            scheduler.shutdownNow();
            workerPool.shutdownNow();
        }
    }

    // -------------------- Consumer runtime --------------------

    private class ConsumerRuntime {
        final String topic;
        final String groupId;
        final MessageProcessor processor;
        final ConsumerProperties props;
        final List<PartitionWorker> workers = new ArrayList<>();
        final AtomicBoolean running = new AtomicBoolean(false);

        ConsumerRuntime(String topic, String groupId, MessageProcessor processor, ConsumerProperties props) {
            this.topic = topic;
            this.groupId = groupId;
            this.processor = processor;
            this.props = props;
        }

        void start() {
            running.set(true);
            // Ensure consumer_offsets rows exist for partitions
            for (int p = 0; p < props.partitions; p++) {
                try {
                    jdbc.update("INSERT INTO consumer_offsets(topic, group_id, partition, last_consumed_id) " +
                                    "VALUES (?, ?, ?, 0) ON CONFLICT (topic, group_id, partition) DO NOTHING",
                            topic, groupId, p);
                } catch (Exception e) {
                    log.debug("consumer_offsets insert ignored: {}", e.getMessage());
                }
            }

            for (int i = 0; i < props.workerThreads; i++) {
                PartitionWorker w = new PartitionWorker(topic, groupId, processor, props);
                workers.add(w);
                // schedule worker to run asynchronously
                workerPool.submit(w);
            }
            log.info("Started consumer runtime topic={} group={} workers={}", topic, groupId, props.workerThreads);
        }

        void stop() {
            running.set(false);
            workers.forEach(PartitionWorker::stop);
            log.info("Stopped consumer runtime topic={} group={}", topic, groupId);
        }
    }

    // -------------------- PartitionWorker --------------------

    private class PartitionWorker implements Runnable {
        private final String topic;
        private final String groupId;
        private final MessageProcessor processor;
        private final ConsumerProperties props;
        private final AtomicBoolean running = new AtomicBoolean(true);

        PartitionWorker(String topic, String groupId, MessageProcessor processor, ConsumerProperties props) {
            this.topic = topic;
            this.groupId = groupId;
            this.processor = processor;
            this.props = props;
        }

        @Override
        public void run() {
            // Each worker loops trying to claim partitions and process them
            while (running.get()) {
                boolean claimedAny = false;
                for (int partition = 0; partition < props.partitions && running.get(); partition++) {
                    long lockKey = computeAdvisoryKey(topic, groupId, partition);
                    Connection lockConn = null;
                    try {
                        lockConn = dataSource.getConnection();
                        lockConn.setAutoCommit(true); // advisory lock works on session not transaction
                        if (!tryAdvisoryLock(lockConn, lockKey)) {
                            // couldn't claim partition
                            safeClose(lockConn);
                            continue;
                        }
                        // We hold the advisory lock for this partition on lockConn
                        claimedAny = true;
                        // Process partition until we give up or lock lost
                        try {
                            processPartitionLoop(partition, lockConn);
                        } finally {
                            releaseAdvisoryLock(lockConn, lockKey);
                            safeClose(lockConn);
                        }
                    } catch (SQLException sqe) {
                        log.warn("Failed to claim/process partition {} for {}/{}: {}", partition, topic, groupId, sqe.getMessage());
                        safeClose(lockConn);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } catch (Throwable t) {
                        log.error("Worker fatal error", t);
                        safeClose(lockConn);
                    }
                }

                if (!claimedAny) {
                    try {
                        Thread.sleep(props.idleDelayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        void stop() {
            running.set(false);
        }

        private void processPartitionLoop(int partition, Connection lockConn) {
            // Keep processing while running and lock held
            while (running.get() && isLockHeld(lockConn, computeAdvisoryKey(topic, groupId, partition))) {
                try {
                    List<MessageRow> batch = fetchBatch(partition, props.pollBatchSize);
                    if (batch.isEmpty()) {
                        Thread.sleep(props.idleDelayMillis);
                        continue;
                    }
                    for (MessageRow row : batch) {
                        try {
                            boolean ok = processSingle(row, partition);
                            if (!ok) {
                                // processing failed; proceed to next (retries handled)
                            }
                        } catch (Throwable t) {
                            log.warn("Error processing message id {}: {}", row.id, t.getMessage());
                        }
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    log.warn("Error in partition processing loop: {}", ex.getMessage(), ex);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        private List<MessageRow> fetchBatch(int partition, int batchSize) {
            final String sql = "SELECT id, key, payload, attempts FROM broker_messages " +
                    "WHERE topic = ? AND partition = ? AND status = 'READY' AND available_at <= now() " +
                    "ORDER BY id LIMIT ? FOR UPDATE SKIP LOCKED";
            return jdbc.query(sql, new Object[]{topic, partition, batchSize}, (rs, rn) -> {
                MessageRow r = new MessageRow();
                r.id = rs.getLong("id");
                r.key = rs.getString("key");
                r.payloadJson = rs.getString("payload");
                r.attempts = rs.getInt("attempts");
                return r;
            });
        }

        private boolean processSingle(MessageRow row, int partition) {
            long msgId = row.id;
            Map<String, String> payload;
            try {
                //noinspection unchecked
                payload = mapper.readValue(row.payloadJson, Map.class);
            } catch (Exception e) {
                log.error("Invalid JSON for message id {}: {}", msgId, e.getMessage());
                moveToDlqTransactional(msgId, partition, row.key, row.payloadJson, "invalid-json", row.attempts);
                return false;
            }

            try {
                // Ensure we lock/check state in a short transaction before processing
                tx.execute(txStatus -> {
                    Map<String, Object> info = jdbc.queryForMap("SELECT attempts, status FROM broker_messages WHERE id = ? FOR UPDATE", msgId);
                    String currStatus = (String) info.get("status");
                    if (!"READY".equals(currStatus) && !"PROCESSING".equals(currStatus)) {
                        // already handled
                        return null;
                    }
                    jdbc.update("UPDATE broker_messages SET status = 'PROCESSING' WHERE id = ?", msgId);
                    return null;
                });

                // Process outside transaction to avoid holding DB locks during long work
                processor.process(payload);

                // On success: finalize in transaction
                tx.execute(txStatus -> {
                    jdbc.update("UPDATE broker_messages SET status = 'DONE' WHERE id = ?", msgId);
                    jdbc.update("INSERT INTO consumer_offsets(topic, group_id, partition, last_consumed_id, updated_at) " +
                                    "VALUES (?, ?, ?, ?, now()) " +
                                    "ON CONFLICT (topic, group_id, partition) DO UPDATE SET last_consumed_id = GREATEST(consumer_offsets.last_consumed_id, EXCLUDED.last_consumed_id), updated_at = now()",
                            topic, groupId, partition, msgId);
                    return null;
                });

                meter.counter("broker.process.success", "topic", topic).increment();
                return true;
            } catch (Exception ex) {
                meter.counter("broker.process.failure", "topic", topic).increment();
                // handle retry/backoff or DLQ in transactional block
                try {
                    tx.execute(txStatus -> {
                        Map<String, Object> info = jdbc.queryForMap("SELECT attempts FROM broker_messages WHERE id = ? FOR UPDATE", msgId);
                        int attempts = ((Number) info.get("attempts")).intValue();
                        attempts++;
                        if (attempts >= props.maxAttempts) {
                            // move to DLQ and delete original
                            jdbc.update("INSERT INTO broker_dlq(original_message_id, topic, partition, key, payload, error, attempts) VALUES (?, ?, ?, ?, cast(? as jsonb), ?, ?)",
                                    msgId, topic, partition, row.key, row.payloadJson, ex.getMessage(), attempts);
                            jdbc.update("DELETE FROM broker_messages WHERE id = ?", msgId);
                            meter.counter("broker.dlq.sent", "topic", topic).increment();
                        } else {
                            long backoff = computeBackoffMillis(attempts, props.baseBackoffMillis, props.maxBackoffMillis);
                            jdbc.update("UPDATE broker_messages SET attempts = ?, status = 'READY', available_at = now() + (? || ' milliseconds')::interval WHERE id = ?",
                                    attempts, backoff, msgId);
                        }
                        return null;
                    });
                } catch (Exception e2) {
                    log.error("Failed to update retry/DLQ state for message {}: {}", msgId, e2.getMessage(), e2);
                }
                return false;
            }
        }

        private void moveToDlqTransactional(long msgId, int partition, String key, String payloadJson, String error, int attempts) {
            try {
                tx.execute(txStatus -> {
                    jdbc.update("INSERT INTO broker_dlq(original_message_id, topic, partition, key, payload, error, attempts) VALUES (?, ?, ?, ?, cast(? as jsonb), ?, ?)",
                            msgId, topic, partition, key, payloadJson, error, attempts);
                    jdbc.update("DELETE FROM broker_messages WHERE id = ?", msgId);
                    meter.counter("broker.dlq.sent", "topic", topic).increment();
                    return null;
                });
            } catch (Exception e) {
                log.error("Failed to move corrupt message {} to DLQ: {}", msgId, e.getMessage(), e);
            }
        }

        private long computeBackoffMillis(int attempts, long base, long max) {
            long b = base * (1L << Math.max(0, attempts - 1));
            return Math.min(b, max);
        }

        // ---------------- advisory locks via dedicated Connection ----------------

        private boolean tryAdvisoryLock(Connection conn, long key) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
                ps.setLong(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean(1);
                    }
                }
            } catch (SQLException e) {
                log.warn("pg_try_advisory_lock failed: {}", e.getMessage());
            }
            return false;
        }

        private boolean isLockHeld(Connection conn, long key) {
            // We assume the lock is held on this session if connection is usable.
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1")) {
                ps.execute();
                return true;
            } catch (SQLException e) {
                return false;
            }
        }

        private void releaseAdvisoryLock(Connection conn, long key) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT pg_advisory_unlock(?)")) {
                ps.setLong(1, key);
                ps.execute();
            } catch (SQLException e) {
                log.warn("pg_advisory_unlock failed: {}", e.getMessage());
            }
        }

        private void safeClose(Connection conn) {
            if (conn == null) return;
            try {
                conn.close();
            } catch (SQLException e) {
                log.debug("Failed to close lock connection: {}", e.getMessage());
            }
        }
    }

    // -------------------- utils --------------------

    private static long computeAdvisoryKey(String topic, String groupId, int partition) {
        // deterministic combination to a 64-bit signed value
        long h1 = topic == null ? 0L : topic.hashCode() & 0xffffffffL;
        long h2 = groupId == null ? 0L : groupId.hashCode() & 0xffffffffL;
        return (h1 << 32) ^ (h2 << 1) ^ (partition & 0xffffffffL);
    }

    private static void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static class MessageRow {
        long id;
        String key;
        String payloadJson;
        int attempts;
    }
}