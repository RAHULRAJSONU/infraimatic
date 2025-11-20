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

CREATE INDEX idx_broker_messages_ready
    ON broker_messages(topic, partition, status, available_at);

CREATE INDEX idx_broker_messages_available
    ON broker_messages(available_at);