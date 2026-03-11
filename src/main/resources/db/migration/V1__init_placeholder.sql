CREATE TABLE authorizations (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    patient_document VARCHAR(50) NOT NULL,
    member_number VARCHAR(50) NOT NULL,
    plan_code VARCHAR(50) NOT NULL,
    practice_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    updated_at DATETIMEOFFSET NOT NULL
);

CREATE INDEX idx_authorizations_member_number ON authorizations (member_number);
CREATE INDEX idx_authorizations_plan_code ON authorizations (plan_code);
CREATE INDEX idx_authorizations_status ON authorizations (status);
