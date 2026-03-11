CREATE TABLE outbox_events (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    aggregate_id UNIQUEIDENTIFIER NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_json VARCHAR(MAX) NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    published_at DATETIMEOFFSET NULL,
    correlation_id VARCHAR(100) NOT NULL
);

CREATE INDEX idx_outbox_events_status_created_at ON outbox_events (status, created_at);
CREATE INDEX idx_outbox_events_aggregate_id ON outbox_events (aggregate_id);
CREATE INDEX idx_outbox_events_correlation_id ON outbox_events (correlation_id);
