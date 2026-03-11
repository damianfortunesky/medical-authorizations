CREATE TABLE processed_messages (
    message_id VARCHAR(120) NOT NULL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    processed_at DATETIMEOFFSET NOT NULL
);

CREATE INDEX idx_processed_messages_processed_at ON processed_messages (processed_at);

CREATE TABLE authorization_status_history (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    authorization_id UNIQUEIDENTIFIER NOT NULL,
    from_status VARCHAR(20) NOT NULL,
    to_status VARCHAR(20) NOT NULL,
    changed_at DATETIMEOFFSET NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    CONSTRAINT fk_authorization_status_history_authorization
        FOREIGN KEY (authorization_id) REFERENCES authorizations (id)
);

CREATE INDEX idx_auth_status_history_auth_id_changed_at
    ON authorization_status_history (authorization_id, changed_at);
