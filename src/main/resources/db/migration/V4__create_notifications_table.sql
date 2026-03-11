CREATE TABLE notifications (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    authorization_id UNIQUEIDENTIFIER NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    patient_document VARCHAR(50) NOT NULL,
    member_number VARCHAR(50) NOT NULL,
    plan_code VARCHAR(50) NOT NULL,
    practice_code VARCHAR(50) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    sent_at DATETIMEOFFSET NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT fk_notifications_authorization
        FOREIGN KEY (authorization_id) REFERENCES authorizations (id)
);

CREATE INDEX idx_notifications_authorization_id ON notifications (authorization_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notifications_correlation_id ON notifications (correlation_id);
