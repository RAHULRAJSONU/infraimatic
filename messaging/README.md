# 🚀 Active Java Messaging Service

### A Fault-Tolerant, Partitioned, Durable, High-Throughput Message Broker Built on PostgreSQL

This project implements a **Kafka-like message broker in pure Java**, using **PostgreSQL advisory locks**, **partition claims**, **retry & DLQ**, **backpressure**, and **parallel workers** — without Kafka, RabbitMQ, or Redis.

Designed for:

* WhatsApp Chatbot Processing Pipelines
* Event-driven Microservices
* ETL / Workflow Orchestration
* Guaranteed delivery with retries + DLQ
* Cloud or on-prem setups where Kafka is unavailable

Powered by:

* **Spring Boot 3.5+**
* **Java 25**
* **PostgreSQL 14+**
* **Micrometer / Prometheus**
* **Spring JDBC**

---

# ⭐ Features

### ✔ Guaranteed‐Delivery Message Broker

Backed by Postgres tables (`broker_messages`, `consumer_offsets`, `broker_dlq`).

### ✔ Partitioning Like Kafka

Producers assign messages to partitions via hash or custom partitioning logic.
Consumers claim partitions via Postgres **advisory locks**.

### ✔ Horizontal Scalability

Multiple instances of the same consumer group auto-balance partitions.

### ✔ Retries + Exponential Backoff + DLQ

Built-in:

* Retry counter
* Exponential backoff
* Move to DLQ table after `maxAttempts`

### ✔ High Concurrency

Workers run in:

* Cached thread pool
* Partition workers per consumer group
* Atomic partition locking

### ✔ Pure Java, Zero External Brokers Needed

No Kafka, no Redis, no Zookeeper.

### ✔ Observability

* Publish latency
* Process success/failure counts
* DLQ counts
* Consumer count
* Prometheus metrics via actuator

---

# 🏗️ Architecture Overview

```
Producer → broker_messages (Postgres) → Consumers → DLQ (if failed)
           ^ advisory locks for partitions ^
```

**Incoming flow:**

1. `IncomingMessageService` stores incoming Twilio/WhatsApp message → publishes to broker.
2. Broker assigns message to partition.
3. Consumer runtime:

    * Claims partitions using advisory locks.
    * Polls `broker_messages`.
    * Calls your `MessageProcessor`.
    * Writes offsets and final status.

---

# 📦 Required Database Schema

Run these SQL migrations before starting the application:

```sql
CREATE TABLE broker_messages (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    partition INT NOT NULL,
    key VARCHAR(255),
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INT DEFAULT 0,
    available_at TIMESTAMP DEFAULT now()
);

CREATE TABLE consumer_offsets (
    topic VARCHAR(255) NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    partition INT NOT NULL,
    last_consumed_id BIGINT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (topic, group_id, partition)
);

CREATE TABLE broker_dlq (
    id BIGSERIAL PRIMARY KEY,
    original_message_id BIGINT,
    topic VARCHAR(255),
    partition INT,
    key VARCHAR(255),
    payload JSONB,
    error TEXT,
    attempts INT,
    created_at TIMESTAMP DEFAULT now()
);
```

Indexes (recommended):

```sql
CREATE INDEX idx_broker_messages_ready 
    ON broker_messages(topic, partition, status, available_at);

CREATE INDEX idx_broker_messages_available 
    ON broker_messages(available_at);
```

---

# ⚙️ Required Application Properties

Add to `application.yml`:

```yaml
broker:
  partitions: 8

  consumer:
    group: whatsapp-processor
    workers: 4
    batch-size: 20
    max-attempts: 5
    base-backoff-millis: 500
    max-backoff-millis: 30000
    idle-delay-millis: 200
    poll-interval-ms: 500

management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus
  endpoint:
    health:
      show-details: when_authorized
  metrics:
    export:
      prometheus:
        enabled: true

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/infraimatic
    username: infra
    password: infra_password
    driver-class-name: org.postgresql.Driver
```

---

# 🧩 How to Use in Your Application

## 1. Produce Messages (send to topic)

Use `IncomingMessageService`:

```java
incomingMessageService.handleIncoming(payload);
```

This will:

1. Save your business entity (MessageEntity)
2. Publish to broker topic `"whatsapp.incoming"`

---

## 2. Consume Messages (automatic parallel processing)

Your consumer automatically starts after the application boots:

```java
@Component
public class IncomingMessageConsumer {
    @EventListener(ApplicationStartedEvent.class)
    public void onStarted() {
        broker.startConsumer(topic, groupId, processor, props);
    }
}
```

Broker calls your `MessageProcessor`:

```java
@Component
public class WhatsAppProcessor implements MessageProcessor {

    @Override
    public void process(Map<String, String> payload) {
        // Implement message business logic here
        // Throw exception to trigger retry
    }
}
```

---

# 🧪 Testing with Postgres Locally

Use Docker:

```bash
docker run -p 5432:5432 \
  -e POSTGRES_USER=infra \
  -e POSTGRES_PASSWORD=infra_password \
  -e POSTGRES_DB=infraimatic \
  postgres:16
```

Then create schema using SQL above.

---

# 🛠️ Tuning & Performance

| Parameter                 | Effect                             |
| ------------------------- | ---------------------------------- |
| `broker.partitions`       | More partitions = more parallelism |
| `broker.consumer.workers` | More threads per instance          |
| `max-attempts`            | Retry attempts before DLQ          |
| `base-backoff-millis`     | Retry backoff multiplier           |
| `idle-delay-millis`       | Delay when no messages found       |

Recommended starting values:

* partitions: **8**
* workers: **4**
* batch-size: **20**
* max-attempts: **5**
* CPU threads × 2 rule for worker total

---

# 🩹 Error Handling

Broker automatically handles:

### ✔ JSON parse errors → DLQ

### ✔ Processor exceptions → retry with backoff

### ✔ Max attempts exceeded → DLQ

### ✔ Partition lock lost → worker reclaims automatically

---

# 🛡️ Observability & Metrics

Available metrics:

| Metric                    | Meaning                          |
| ------------------------- | -------------------------------- |
| `broker.publish.duration` | Message publish time             |
| `broker.process.success`  | Successful message process count |
| `broker.process.failure`  | Failed message process count     |
| `broker.consumer.count`   | Running consumers                |
| `broker.dlq.sent`         | Messages moved to DLQ            |

Prometheus endpoint:

```
GET /actuator/prometheus
```

---

# 🧱 Project Structure

```
src/main/java
  ├─ broker/
  │   ├─ JdbcMessageBroker.java
  │   ├─ MessageBroker.java
  │   ├─ MessageProcessor.java
  │   ├─ ConsumerProperties.java
  │   └─ ConsumerHandle.java
  ├─ whatsapp/
      ├─ IncomingMessageService.java
      ├─ IncomingMessageConsumer.java
      └─ MessageProcessorImplementation.java
```

---

# ✔ Production Checklist

* [ ] Postgres max connections tuned (>= 200)
* [ ] `broker_messages` table partitioned (optional large-scale)
* [ ] CPU threads ≥ workers × consumer threads
* [ ] Prometheus/Grafana installed
* [ ] DLQ alerts configured

---

If you want, I can also generate:

✅ A full `docker-compose.yml` (App + Postgres + pgAdmin)
✅ A DB migration (Liquibase / Flyway) for all broker tables
✅ A Grafana dashboard for broker metrics
✅ Autoscaling tips & production tuning guide

Just tell me!
