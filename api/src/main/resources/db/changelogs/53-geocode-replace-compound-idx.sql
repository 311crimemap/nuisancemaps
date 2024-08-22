-- liquibase formatted sql

-- changeset root:1724361623224-1
DROP INDEX IF EXISTS idx_address;

-- changeset root:1724361623224-2
CREATE INDEX idx_source_address ON geocode (source_id, address);
