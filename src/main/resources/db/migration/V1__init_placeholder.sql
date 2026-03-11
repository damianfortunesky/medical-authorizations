CREATE TABLE authorizations (
    id UNIQUEIDENTIFIER NOT NULL,
    member_id NVARCHAR(100) NOT NULL,
    provider_id NVARCHAR(100) NOT NULL,
    procedure_code NVARCHAR(50) NOT NULL,
    status NVARCHAR(50) NOT NULL,
    created_at DATETIMEOFFSET(7) NOT NULL,
    CONSTRAINT pk_authorizations PRIMARY KEY (id)
);

CREATE INDEX idx_authorizations_member_id ON authorizations (member_id);
CREATE INDEX idx_authorizations_provider_id ON authorizations (provider_id);
CREATE INDEX idx_authorizations_status ON authorizations (status);
